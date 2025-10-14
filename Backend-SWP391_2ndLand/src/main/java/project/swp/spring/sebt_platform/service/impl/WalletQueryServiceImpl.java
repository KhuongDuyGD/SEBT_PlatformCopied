package project.swp.spring.sebt_platform.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.WalletQueryService;

@Service
public class WalletQueryServiceImpl implements WalletQueryService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletQueryServiceImpl(WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    public WalletEntity getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Override
    public Page<WalletTransactionEntity> getTransactions(Long userId, WalletPurpose purpose, Pageable pageable) {
        if (purpose != null) {
            return walletTransactionRepository.findByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose, pageable);
        }
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public WalletTransactionEntity getTransactionByOrderId(String orderId) {
        return walletTransactionRepository.findByOrderId(orderId);
    }
}
