package project.swp.spring.sebt_platform.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface VnpayService {
    record TopUpIntent(String orderId, String paymentUrl, Double amount, java.time.OffsetDateTime expiresAt) {}

    String createPaymentUrl(double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException;
    boolean validateReturn(Map<String, String> params) throws UnsupportedEncodingException;
    void updateTransactionStatus(String orderId, boolean isSuccess);
    void handleRefund(double amount, Long userId, HttpServletRequest request, boolean isRollback) throws IOException;

    TopUpIntent createTopUpIntent(Double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException;
}