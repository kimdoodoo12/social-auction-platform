package com.socialauction.backend.auction.queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.service.ClosingAuctionService;
import com.socialauction.backend.auction.user.service.UserAuctionService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionQueue {

    private final ClosingAuctionService closingAuctionService;

    @Value("${auction.queue.worker-count:4}")
    private int workerCount;

    @Value("${auction.queue.max-pending:500}")
    private int maxPending;

    // * 여러 요청 스레드가 넣고 여러 작업 스레드가 꺼내는 공유 큐다.
    // * ArrayBlockingQueue가 큐의 동시 접근을 처리한다.
    private BlockingQueue<Command> pending;

    // * 경매 ID마다 하나의 락을 사용한다.
    // * 서로 다른 경매 ID는 서로 다른 락을 사용하므로 병렬 실행된다.
    private final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks =
        new ConcurrentHashMap<>();

    private ExecutorService workers;

    // * Spring 설정값 주입이 끝난 뒤 큐와 작업 스레드를 만든다.
    @PostConstruct
    public void start() {
        if (workerCount < 1 || maxPending < 1) {
            throw new IllegalArgumentException(
                "경매 큐 설정은 1 이상이어야 합니다."
            );
        }

        pending = new ArrayBlockingQueue<>(maxPending);
        workers = Executors.newFixedThreadPool(workerCount);

        for (int i = 0; i < workerCount; i++) {
            workers.submit(this::workerLoop);
        }
    }

    // * 입찰 요청을 공유 큐에 넣고, 처리 결과를 받을 Future를 반환한다.
    public CompletableFuture<Boolean> submitBid(
        AuctionConfirmDto bid,
        Supplier<Boolean> processBid
    ) {
    CompletableFuture<Boolean> result = new CompletableFuture<>();

    enqueue(new Command(
        bid.getAuctionId(),
        () -> result.complete(processBid.get()),
        result::completeExceptionally
    ));

    return result;
}

    // * 경매 종료 요청도 같은 공유 큐에 넣는다.
    public CompletableFuture<Void> submitClose(int auctionId) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        enqueue(new Command(
            auctionId,
            () -> {
                closingAuctionService.closeAuction(auctionId);
                result.complete(null);
            },
            result::completeExceptionally
        ));

        return result;
    }

    // * 큐에 자리가 있으면 즉시 등록하고, 가득 찼으면 요청을 거절한다.
    private void enqueue(Command command) {
        if (workers == null || workers.isShutdown()) {
            throw new RejectedExecutionException(
                "경매 큐를 사용할 수 없습니다."
            );
        }

        if (!pending.offer(command)) {
            throw new QueueFullException();
        }

        // ! 로그
        log.info("QUEUE IN  auctionId={} pending={}",
            command.auctionId(), pending.size());
    }

    // * 여러 작업 스레드가 이 메서드를 각각 실행한다.
    // * 큐에서 요청을 꺼낸 뒤 해당 경매 ID의 락을 잡고 서비스를 호출한다.
    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            Command command = null;

            try {
                command = pending.take();
                // ! 로그
                log.info("QUEUE OUT auctionId={} thread={}",
                    command.auctionId(), Thread.currentThread().getName());

                ReentrantLock auctionLock =
                    auctionLocks.computeIfAbsent(
                        command.auctionId(),
                        id -> new ReentrantLock()
                    );

                // * 같은 경매가 처리 중이면 여기서 기다린다.
                // * 다른 경매 ID의 락과는 독립적이다.
                auctionLock.lockInterruptibly();
                // ! 로그
                log.info("BID START auctionId={} thread={}",
                    command.auctionId(), Thread.currentThread().getName());
                try {
                    // * 별도 Spring 서비스 Bean을 호출한다.
                    // * @Transactional 서비스 메서드가 반환될 때 커밋도 완료된다.
                    command.action().run();
                    // ! 로그
                    log.info("BID END   auctionId={} thread={}",
                        command.auctionId(), Thread.currentThread().getName());
                } catch (RuntimeException e) {
                    command.onFailure().accept(e);
                } finally {
                    auctionLock.unlock();
                }

            } catch (InterruptedException e) {
                // * 서버 종료 중 큐 또는 락을 기다리다 중단된 요청에 실패를 알린다.
                if (command != null) {
                    command.onFailure().accept(e);
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    // * 서버 종료 시 작업 스레드를 중단하고 큐에 남은 요청에 실패를 알린다.
    @PreDestroy
    public void stop() {
        if (workers == null) {
            return;
        }

        workers.shutdownNow();

        Command command;
        while ((command = pending.poll()) != null) {
            command.onFailure().accept(
                new RejectedExecutionException("경매 서버가 종료되었습니다.")
            );
        }
    }

    // * 큐에 저장할 작업 한 건이다.
    // * action은 서비스 호출, onFailure는 실패 결과 전달을 담당한다.
    private record Command(
        int auctionId,
        Runnable action,
        Consumer<Throwable> onFailure
    ) {}

    public static class QueueFullException extends RuntimeException {
        public QueueFullException() {
            super("경매 요청이 많아 잠시 후 다시 시도해 주세요.");
        }
    }
}
