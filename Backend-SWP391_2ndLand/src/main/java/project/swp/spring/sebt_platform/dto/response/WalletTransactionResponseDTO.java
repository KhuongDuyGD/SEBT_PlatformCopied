package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.model.WalletTransactionEntity;

import java.math.BigDecimal;

public record WalletTransactionResponseDTO(String orderId,
                                           BigDecimal amount,
                                           BigDecimal balanceBefore,
                                           BigDecimal balanceAfter,
                                           String status,
                                           String purpose,
                                           String entryType,
                                           java.time.LocalDateTime createdAt,
                                           String description) {
    public static WalletTransactionResponseDTO from(WalletTransactionEntity e) {
        return new WalletTransactionResponseDTO(
                e.getOrderId(),
                e.getAmount(),
                e.getBalanceBefore(),
                e.getBalanceAfter(),
                e.getStatus().name(),
                e.getPurpose() != null ? e.getPurpose().name() : null,
                e.getEntryType() != null ? e.getEntryType().name() : null,
                e.getCreatedAt(),
                e.getDescription()
        );
    }
}
