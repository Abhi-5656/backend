package com.wfm.experts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Enable Spring's asynchronous method execution capability
public class AsyncConfig {

    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Adjust these pool sizes based on your expected load and server resources
        executor.setCorePoolSize(10); // Number of threads to keep in the pool
        executor.setMaxPoolSize(50);  // Maximum number of threads allowed
        executor.setQueueCapacity(1000); // Queue for tasks when all core threads are busy
        executor.setThreadNamePrefix("NotifAsync-");
        executor.initialize();
        return executor;
    }
}