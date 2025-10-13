package project.swp.spring.sebt_platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VnpayConfig {

    @Value("${vnpay.tmn-Code}")
    private String tmnCode;

    @Value("${vnpay.hash-Secret}")
    private String hashSecret;

    @Value("${vnpay.pay-Url}")
    private String payUrl;

    @Value("${vnpay.api-Url}")
    private String returnUrl;

    @Value("${vnpay.return-Url}")
    private String apiUrl;

    // Getters & Setters
    public String getTmnCode() { return tmnCode; }
    public void setTmnCode(String tmnCode) { this.tmnCode = tmnCode; }

    public String getHashSecret() { return hashSecret; }
    public void setHashSecret(String hashSecret) { this.hashSecret = hashSecret; }

    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
}
