package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;

public interface WalletQueryService {
    WalletEntity getWalletByUserId(Long userId);
    Page<WalletTransactionEntity> getTransactions(Long userId, WalletPurpose purpose, Pageable pageable);
    WalletTransactionEntity getTransactionByOrderId(String orderId);
}
