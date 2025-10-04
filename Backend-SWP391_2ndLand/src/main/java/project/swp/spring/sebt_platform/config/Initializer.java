package project.swp.spring.sebt_platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.swp.spring.sebt_platform.service.AuthService;

@Configuration
public class Initializer {

    @Autowired
    private AuthService authService;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            // Initialize database with default locations if needed
            authService.register("admin123" ,"noreplysebtplatform@gmail.com");
            authService.register("123456" ,"ducminh852005@gmail.com");
        };
    }
}
