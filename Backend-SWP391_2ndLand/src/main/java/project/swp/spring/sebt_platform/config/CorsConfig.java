package project.swp.spring.sebt_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc  // Thêm nếu cần, nhưng thường không bắt buộc ở Spring Boot 3
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Áp dụng cho tất cả API (/api/** cũng được nếu chỉ muốn giới hạn)
                .allowedOrigins("http://localhost:5173")  // Origin của Vite frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")  // Methods bạn dùng (POST cho register)
                .allowedHeaders("*")  // Cho phép tất cả header (bao gồm Content-Type cho JSON)
                .exposedHeaders("*")  // Nếu cần expose header custom
                .allowCredentials(true)  // Quan trọng vì bạn dùng session/cookie
                .maxAge(3600);  // Cache preflight 1 giờ
    }
}