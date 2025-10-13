package project.swp.spring.sebt_platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AiConfig {
    @Value("${app.ai.gemini.apiKey:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    // New configurable knobs
    @Value("${app.pricing.clamp.percent:0.15}")
    private double clampPercent; // e.g. 0.15 = ±15%
    @Value("${app.pricing.retry.attempts:3}")
    private int retryAttempts; // total attempts including first
    @Value("${app.pricing.retry.initialDelayMillis:400}")
    private long initialDelayMillis; // backoff base
    @Value("${app.pricing.fallback.model:gemini-1.5-flash-latest}")
    private String fallbackModel;
    @Value("${app.pricing.cache.enabled:true}")
    private boolean cacheEnabled;
    @Value("${app.pricing.cache.maxSize:500}")
    private int cacheMaxSize;

    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public void setGeminiModel(String geminiModel) {
        this.geminiModel = geminiModel;
    }

    public double getClampPercent() {
        return clampPercent;
    }

    public void setClampPercent(double clampPercent) {
        this.clampPercent = clampPercent;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public long getInitialDelayMillis() {
        return initialDelayMillis;
    }

    public void setInitialDelayMillis(long initialDelayMillis) {
        this.initialDelayMillis = initialDelayMillis;
    }

    public String getFallbackModel() {
        return fallbackModel;
    }

    public void setFallbackModel(String fallbackModel) {
        this.fallbackModel = fallbackModel;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }
}
