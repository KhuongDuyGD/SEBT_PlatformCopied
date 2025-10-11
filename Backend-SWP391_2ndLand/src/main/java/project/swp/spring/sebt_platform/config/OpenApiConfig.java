package project.swp.spring.sebt_platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        Components components = new Components()
                .addSecuritySchemes("sessionCookie", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID"))
                .addSchemas("ErrorResponse", new Schema<>()
                        .addProperty("timestamp", new StringSchema().format("date-time"))
                        .addProperty("path", new StringSchema())
                        .addProperty("status", new IntegerSchema())
                        .addProperty("error", new StringSchema())
                        .addProperty("message", new StringSchema())
                        .addProperty("code", new StringSchema())
                );

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("SEBT_platform API")
                        .description("Tài liệu REST API cho hệ thống SEBT_platform Spring Boot")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nguyen Phuoc Duc Minh")
                                .email("ducminh852005@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
