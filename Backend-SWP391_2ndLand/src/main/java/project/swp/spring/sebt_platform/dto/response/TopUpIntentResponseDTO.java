package project.swp.spring.sebt_platform.dto.response;

public record TopUpIntentResponseDTO(String orderId, String paymentUrl, Double amount,
                                     java.time.OffsetDateTime expiresAt) {
}