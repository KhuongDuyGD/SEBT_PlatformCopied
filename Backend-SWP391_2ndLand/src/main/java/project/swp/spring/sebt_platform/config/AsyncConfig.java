// src/main/java/project/swp/spring/sebtplatform/config/AsyncConfig.java
package project.swp.spring.sebt_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncMail-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "imageUploadExecutor")
    public Executor imageUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);        // Ít hơn vì upload ảnh tốn I/O
        executor.setMaxPoolSize(8);         // Max threads cho upload
        executor.setQueueCapacity(50);      // Queue size cho upload tasks
        executor.setThreadNamePrefix("AsyncImageUpload-");
        executor.setKeepAliveSeconds(60);   // Keep alive time
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}