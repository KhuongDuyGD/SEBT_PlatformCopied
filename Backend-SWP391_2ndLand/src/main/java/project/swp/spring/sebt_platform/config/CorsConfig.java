package project.swp.spring.sebt_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        // Cho phép tất cả các port dev Vite (5173, 5174, ...) và có thể mở rộng thêm
        .allowedOriginPatterns("http://localhost:*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
        .allowedHeaders("*")
        .exposedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
    }
}
