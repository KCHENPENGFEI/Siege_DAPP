package com.example.zjusiege;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {

    @Override
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        threadPool.setCorePoolSize(5);
        // 设置最大线程数
        threadPool.setMaxPoolSize(100);
        // 等待队列长度
        threadPool.setQueueCapacity(25);
        // 等待时间 （默认为0，此时立即停止），并每等60秒后强制停止
        threadPool.setAwaitTerminationSeconds(60);
        threadPool.initialize();
        return threadPool;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
