package project.swp.spring.sebt_platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Test component để verify rằng Spring đã load được biến môi trường từ .env
 */
@Component
public class EnvVariableVerifier implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EnvVariableVerifier.class);

    @Value("${CLOUDINARY_CLOUD_NAME:NOT_LOADED}")
    private String cloudinaryCloudName;

    @Value("${CLOUDINARY_API_KEY:NOT_LOADED}")
    private String cloudinaryApiKey;

    @Value("${DB_HOST:NOT_LOADED}")
    private String dbHost;

    @Value("${DB_NAME:NOT_LOADED}")
    private String dbName;

    @Value("${MAIL_USERNAME:NOT_LOADED}")
    private String mailUsername;

    @Value("${APP_ENV:NOT_LOADED}")
    private String appEnv;

    @Override
    public void run(String... args) {

        boolean allLoaded = true;

        // Check Cloudinary
        if (!"NOT_LOADED".equals(cloudinaryCloudName)) {
            logger.info("CLOUDINARY_CLOUD_NAME: {}", cloudinaryCloudName);
        } else {
            logger.error("CLOUDINARY_CLOUD_NAME: NOT LOADED");
            allLoaded = false;
        }

        if (!"NOT_LOADED".equals(cloudinaryApiKey)) {
            logger.info("CLOUDINARY_API_KEY: {}****", cloudinaryApiKey.substring(0, Math.min(8, cloudinaryApiKey.length())));
        } else {
            logger.error("CLOUDINARY_API_KEY: NOT LOADED");
            allLoaded = false;
        }

        // Check Database
        if (!"NOT_LOADED".equals(dbHost)) {
            logger.info("DB_HOST: {}", dbHost);
        } else {
            logger.error("DB_HOST: NOT LOADED");
            allLoaded = false;
        }

        if (!"NOT_LOADED".equals(dbName)) {
            logger.info("DB_NAME: {}", dbName);
        } else {
            logger.error("DB_NAME: NOT LOADED");
            allLoaded = false;
        }

        // Check Email
        if (!"NOT_LOADED".equals(mailUsername)) {
            logger.info("MAIL_USERNAME: {}", mailUsername);
        } else {
            logger.error("MAIL_USERNAME: NOT LOADED");
            allLoaded = false;
        }

        // Check App Env
        if (!"NOT_LOADED".equals(appEnv)) {
            logger.info("APP_ENV: {}", appEnv);
        } else {
            logger.error("APP_ENV: NOT LOADED");
            allLoaded = false;
        }

        logger.info("========================================");
        if (allLoaded) {
            logger.info("ALL ENVIRONMENT VARIABLES LOADED SUCCESSFULLY!");
        } else {
            logger.warn("SOME ENVIRONMENT VARIABLES NOT LOADED!");
            logger.warn("Check if .env file exists in project root");
        }
        logger.info("========================================");
    }
}
