package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.model.WalletTransactionEntity;

import java.math.BigDecimal;

public interface WalletLedgerService {
    /**
     * Create a pending top-up transaction and return the entity.
     */
    WalletTransactionEntity createPendingTopUp(Long userId, String orderId, BigDecimal amount);

    /**
     * Complete a pending top-up (idempotent). Returns the updated transaction or existing completed one.
     */
    WalletTransactionEntity completeTopUp(String orderId, boolean success, String metadataJson, BigDecimal callbackAmount);

    /**
     * Debit listing publication fee atomically and persist a ledger entry.
     * @param userId owner of the wallet
     * @param listingId listing id (for metadata linkage)
     * @param fee amount to debit (>0)
     * @return created transaction entity
     */
    WalletTransactionEntity debitListingFee(Long userId, Long listingId, java.math.BigDecimal fee);
}
