package com.assessment.transaction_categorisation_engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "categorizationExecutor")
    public Executor categorizationExecutor(CategorizationProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsync().getCoreSize());
        executor.setMaxPoolSize(properties.getAsync().getMaxSize());
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("categorize-");
        executor.initialize();
        return executor;
    }
}
