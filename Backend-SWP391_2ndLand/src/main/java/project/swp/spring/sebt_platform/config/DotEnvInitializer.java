package project.swp.spring.sebt_platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Đọc file .env và load vào Spring Environment
 * Chạy trước khi Spring Boot khởi tạo beans
 */
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DotEnvInitializer.class);
    private static final String ENV_FILE = ".env";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            File envFile = new File(ENV_FILE);

            if (!envFile.exists()) {
                logger.warn("File .env not found at: {}", envFile.getAbsolutePath());
                logger.warn("Application will use default values from application.properties");
                return;
            }

            logger.info("Loading environment variables from .env file...");

            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(envFile)) {
                properties.load(input);
            }

            Map<String, Object> envMap = new HashMap<>();
            properties.forEach((key, value) -> {
                String keyStr = key.toString();
                String valueStr = value.toString();
                envMap.put(keyStr, valueStr);

                // Log non-sensitive variables
                if (!isSensitive(keyStr)) {
                    logger.debug("Loaded: {} = {}", keyStr, valueStr);
                } else {
                    logger.debug("Loaded: {} = ****", keyStr);
                }
            });

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenv", envMap));

            logger.info("Successfully loaded {} environment variables from .env", envMap.size());

        } catch (IOException e) {
            logger.error("Error reading .env file: {}", e.getMessage());
            logger.warn("Application will continue with default values");
        }
    }

    /**
     * Check if variable name contains sensitive information
     */
    private boolean isSensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password")
            || lowerKey.contains("secret")
            || lowerKey.contains("key")
            || lowerKey.contains("token");
    }
}

