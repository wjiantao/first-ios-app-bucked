package com.shiguang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步执行配置。
 *
 * 推送下发不阻塞点赞/收藏接口，因此用独立线程池执行 {@code @Async("pushExecutor")}；
 * 失败仅记日志，不影响业务成功与否。
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean("pushExecutor")
    public Executor pushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("push-");
        executor.initialize();
        return executor;
    }
}
