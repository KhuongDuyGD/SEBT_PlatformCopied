package project.swp.spring.sebt_platform.dto.response;

import java.math.BigDecimal;

/**
 * DTO trả về kết quả thanh toán phí đăng tin.
 */
public record ListingFeePaymentResponseDTO(
        Long listingId,
        BigDecimal requiredFee,
        boolean paid,
        String statusAfter,
        boolean insufficientBalance,
        BigDecimal walletBalance,
        String message
) {}
