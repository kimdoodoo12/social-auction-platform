package com.socialauction.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration 
public class SchedulerConfig {
    
    @Bean
    public TaskScheduler taskScheduler() {
        // TaskScheduler Thread Pool 생성
        // 여러 상품/경매가 동시에 시작이되면서 taskScheduler에 등록할 때 시간의 차이를 최대한 줄이기 위해 동시에 5개까지 가능
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        // 5개의 쓰레드 풀 생성
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("auction-scheduler");

        return taskScheduler;
    }
}
