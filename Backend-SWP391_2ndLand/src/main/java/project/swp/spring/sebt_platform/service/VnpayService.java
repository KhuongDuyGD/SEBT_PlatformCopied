package project.swp.spring.sebt_platform.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public interface VnpayService {
    record TopUpIntent(String orderId, String paymentUrl, Double amount, OffsetDateTime expiresAt) {}

    /** Result of validating VNPay return */
    record VnpayReturnValidation(boolean validSignature, String orderId, BigDecimal amount, String responseCode, Map<String,String> rawParams) {}

    String createPaymentUrl(double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException;

    VnpayReturnValidation validateReturn(Map<String, String> params) throws UnsupportedEncodingException;

    void updateTransactionStatus(String orderId, boolean isSuccess, BigDecimal amount, Map<String,String> callbackMeta);

    void handleRefund(double amount, Long userId, HttpServletRequest request, boolean isRollback) throws IOException;

    TopUpIntent createTopUpIntent(Double amount, Long userId, HttpServletRequest request) throws UnsupportedEncodingException;
}