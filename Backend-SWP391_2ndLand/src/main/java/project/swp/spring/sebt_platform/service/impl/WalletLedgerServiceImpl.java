package project.swp.spring.sebt_platform.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.swp.spring.sebt_platform.model.SystemConfigEntity;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;
import project.swp.spring.sebt_platform.model.enums.WalletEntryType;
import project.swp.spring.sebt_platform.repository.SystemConfigRepository;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.util.Utils;

import java.math.BigDecimal;

@Service
public class WalletLedgerServiceImpl implements WalletLedgerService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SystemConfigRepository systemConfigRepository;

    public WalletLedgerServiceImpl(WalletRepository walletRepository,
                                   WalletTransactionRepository walletTransactionRepository,
                                   SystemConfigRepository systemConfigRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    @Override
    @Transactional
    public WalletTransactionEntity createPendingTopUp(Long userId, String orderId, BigDecimal amount) {
        WalletTransactionEntity existing = walletTransactionRepository.findByOrderId(orderId);
        if (existing != null) {
            return existing; // idempotent safeguard
        }
        WalletEntity wallet = walletRepository.findByUserId(userId);
        // NOTE: ensure wallet exists (could auto-create if business allows)
        WalletTransactionEntity tx = WalletTransactionEntity.pendingTopUp(orderId, wallet, amount, userId);
        walletTransactionRepository.save(tx);
        return tx;
    }

    @Override
    @Transactional
    public WalletTransactionEntity completeTopUp(String orderId, boolean success, String metadataJson, BigDecimal callbackAmount) {
        WalletTransactionEntity tx = walletTransactionRepository.findByOrderId(orderId);
        if (tx == null) {
            return null;
        }
        if (tx.getStatus() != TransactionStatus.PENDING) {
            return tx; // already processed
        }
        if (success) {
            WalletEntity wallet = tx.getWallet();
            // TODO: Validate external callback amount matches tx.getAmount() to prevent tampering.
            if (callbackAmount != null && tx.getAmount().compareTo(callbackAmount) != 0) {
                // Amount mismatch -> mark failed and do not credit
                tx.setStatus(TransactionStatus.FAILED);
                tx.setDescription("VNPay top-up failed: amount mismatch (expected=" + tx.getAmount() + ", got=" + callbackAmount + ")");
                walletTransactionRepository.save(tx);
                return tx;
            }
            tx.setBalanceBefore(wallet.getBalance());
            wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
            tx.setBalanceAfter(wallet.getBalance());
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setDescription("VNPay top-up completed");
            tx.setMetadata(metadataJson);
            walletRepository.save(wallet); // optimistic lock via @Version
        } else {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setDescription("VNPay top-up failed");
            tx.setMetadata(metadataJson);
        }
        walletTransactionRepository.save(tx);
        return tx;
    }

    @Override
    @Transactional
    public WalletTransactionEntity debitListingFee(Long userId, Long listingId, BigDecimal fee) {
        WalletEntity wallet = walletRepository.findByUserId(userId);
        if (wallet == null) return null;
        if (wallet.getBalance().compareTo(fee) < 0) {
            return null; // or throw new InsufficientFundsException("Insufficient balance for listing fee");
        }
        String orderId = Utils.createOrderId(WalletPurpose.LISTING_FEE, userId);
        WalletTransactionEntity tx = new WalletTransactionEntity();
        tx.setOrderId(orderId);
        tx.setWallet(wallet);
        tx.setAmount(fee.negate()); // store negative to represent debit OR keep positive with entryType=DEBIT
        tx.setBalanceBefore(wallet.getBalance());
        wallet.setBalance(wallet.getBalance().subtract(fee));
        tx.setBalanceAfter(wallet.getBalance());
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setPurpose(WalletPurpose.LISTING_FEE);
        tx.setEntryType(WalletEntryType.DEBIT);
        tx.setUserId(userId);
        tx.setListingId(listingId);
        tx.setDescription("Listing publication fee");
        walletRepository.save(wallet);
        walletTransactionRepository.save(tx);
        return tx;
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

    @Override
    public WalletTransactionEntity pricingFee(Long userId) {
        WalletEntity wallet = walletRepository.findByUserId(userId);
        SystemConfigEntity config = systemConfigRepository.findByPricingFee();
        BigDecimal feeAmount = config != null ? new BigDecimal(config.getConfigValue()) : new BigDecimal("20000.00");
        if (wallet != null && wallet.getBalance().compareTo(feeAmount) >= 0) {
            String orderId = Utils.createOrderId(WalletPurpose.PRICING_FEE, userId);
            WalletTransactionEntity tx = new WalletTransactionEntity();
            tx.setOrderId(orderId);
            tx.setWallet(wallet);
            tx.setAmount(feeAmount.negate()); // store negative to represent debit OR keep positive with entryType=DEBIT
            tx.setBalanceBefore(wallet.getBalance());
            wallet.setBalance(wallet.getBalance().subtract(feeAmount));
            tx.setBalanceAfter(wallet.getBalance());
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setPurpose(WalletPurpose.PRICING_FEE);
            tx.setEntryType(WalletEntryType.DEBIT);
            tx.setUserId(userId);
            tx.setDescription("Pricing feature fee");
            walletRepository.save(wallet);
            walletTransactionRepository.save(tx);
            return tx;
        }
        return null;
    }


}
