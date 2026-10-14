package com.socialauction.backend.global.exception;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.socialauction.backend.auction.queue.AuctionQueue.QueueFullException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestControllerAdvice(basePackages = {
    "com.socialauction.backend.auction",
    "com.socialauction.backend.global.sse"
})
public class AuctionExceptionHandler {
    
    // * [공통 응답 객체] 프론트엔드로 내려줄 JSON 모양 정의 (별도 파일 없이 내부 record로 선언)
    public record ErrorResponse(
        int status,          // HTTP 상태 코드 (예: 400, 409, 500)
        String code,         // 에러 구분 코드 (예: "BAD_REQUEST", "INVALID_STATE")
        String message,      // 서비스에서 throw 할 때 적은 한글 메시지
        LocalDateTime time   // 발생 시각
    ) {
        public static ErrorResponse of(HttpStatus status, String code, String message) {
            return new ErrorResponse(status.value(), code, message, LocalDateTime.now());
        }
    }

        // =========================================================================
        // [처리기 1] 입찰 큐(AuctionQueue) 안에서 던진 예외가 도착하는 곳
        // UserAuctionService.placeBid() 안에서 throw 한 예외는 CompletableFuture 특성상
        // CompletionException 이라는 껍질에 감싸져서 들어옵니다.
        // =========================================================================
        @ExceptionHandler ({CompletionException.class, ExecutionException.class})
        public ResponseEntity<ErrorResponse> handleQueueException(Exception e) {
            // 껍질(CompletionException)을 벗겨서 진짜 알맹이 예외(cause)를 꺼냄
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            // 알맹이가 IllegalArgumentException이면 아래 [처리기 2]로 넘김
            if (cause instanceof IllegalArgumentException ex) {
                return handleBadRequest(ex);
            }
            // 알맹이가 IllegalStateException이면 아래 [처리기 3]으로 넘김
            if (cause instanceof IllegalStateException ex) {
                return handleConflict(ex);
            }
            // 알맹이가 QueueFullException이면 아래 [처리기 5]로 넘김
            if (cause instanceof QueueFullException ex) {
                return handleQueueFull(ex);
            }
            // 알맹이가 ResponseStatusException(Checks.check)이면 아래 [처리기 4]로 넘김
            if (cause instanceof ResponseStatusException ex) {
                return handleResponseStatus(ex);
            }

            // 그 외 알 수 없는 에러면 500 처리
            return handleServerError(e);
        }

    // =========================================================================
    // [처리기 2] throw new IllegalArgumentException("...") 했을 때 도착하는 곳
    // -> HTTP 400 (BAD_REQUEST) 상태 코드와 함께 메시지를 JSON으로 반환
    // =========================================================================
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn("[400 에러] {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage()));
    }

    // =========================================================================
    // [처리기 3] throw new IllegalStateException("...") 했을 때 도착하는 곳
    // -> HTTP 409 (CONFLICT) 상태 코드와 함께 메시지를 JSON으로 반환
    // =========================================================================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException e) {
        log.warn("[409 상태 충돌] {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(HttpStatus.CONFLICT, "INVALID_STATE", e.getMessage()));
    }

    // =========================================================================
    // [처리기 4] 기존 프로젝트의 Checks.check(조건, "메시지") 사용 시 도착하는 곳
    // =========================================================================
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        log.warn("[{} 에러] {}", status.value(), e.getReason());
        return ResponseEntity
            .status(status)
            .body(ErrorResponse.of(status, status.name(), e.getReason()));
    }

    // =========================================================================
    // [처리기 5] AuctionQueue 대기열이 꽉 찼거나(429) 서버 종료 중일 때(503) 도착하는 곳
    // =========================================================================
    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<ErrorResponse> handleQueueFull(QueueFullException e) {
        log.warn("[429 큐 포화] {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, "QUEUE_FULL", e.getMessage()));
    }

    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<ErrorResponse> handleRejected(RejectedExecutionException e) {
        log.error("[503 큐 사용불가] {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, "QUEUE_UNAVAILABLE", e.getMessage()));
    }

    // =========================================================================
    // [처리기 6] 위에서 잡히지 않은 모든 예상치 못한 에러(NullPointerException 등)가 도착하는 곳
    // -> 서버가 죽지 않게 막아주고 HTTP 500으로 안내 메시지 반환
    // =========================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception e) {
        log.error("[500 서버 내부 오류]", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERVER_ERROR",
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
            ));
    }

    

       
}
