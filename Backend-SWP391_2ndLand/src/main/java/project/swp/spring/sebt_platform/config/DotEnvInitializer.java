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

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        logger.info("======= DotEnvInitializer STARTED =======");
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // Try multiple possible locations for .env file
        String[] possiblePaths = {
            ".env",                                    // Current directory
            "../.env",                                 // Parent directory
            "Backend-SWP391_2ndLand/.env",            // Relative from parent
            System.getProperty("user.dir") + "/.env",  // User working directory
            System.getProperty("user.dir") + "/../.env" // Parent of working directory
        };

        File envFile = null;
        for (String path : possiblePaths) {
            File file = new File(path);
            logger.info("Checking for .env at: {}", file.getAbsolutePath());
            if (file.exists() && file.isFile()) {
                envFile = file;
                logger.info("✅ Found .env file at: {}", file.getAbsolutePath());
                break;
            }
        }

        if (envFile == null) {
            logger.error("❌ File .env NOT FOUND in any of the checked locations!");
            logger.error("Current working directory: {}", System.getProperty("user.dir"));
            logger.warn("Application will use default values from application.properties");
            return;
        }

        try {
            logger.info("Loading environment variables from .env file...");

            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(envFile)) {
                properties.load(input);
            }

            Map<String, Object> envMap = new HashMap<>();
            properties.forEach((key, value) -> {
                String keyStr = key.toString().trim();
                String valueStr = value.toString().trim();
                
                // Remove quotes if present
                if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                    valueStr = valueStr.substring(1, valueStr.length() - 1);
                }
                
                envMap.put(keyStr, valueStr);

                // Log non-sensitive variables
                if (!isSensitive(keyStr)) {
                    logger.info("Loaded: {} = {}", keyStr, valueStr);
                } else {
                    logger.info("Loaded: {} = ****", keyStr);
                }
            });

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenv", envMap));

            logger.info("✅ Successfully loaded {} environment variables from .env", envMap.size());

        } catch (IOException e) {
            logger.error("❌ Error reading .env file: {}", e.getMessage(), e);
            logger.warn("Application will continue with default values");
        }
        
        logger.info("======= DotEnvInitializer COMPLETED =======");
    }

    /**
     * Check if variable name contains sensitive information
     */
    private boolean isSensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password")
            || lowerKey.contains("secret")
            || lowerKey.contains("api_key")
            || lowerKey.contains("token");
    }
}
