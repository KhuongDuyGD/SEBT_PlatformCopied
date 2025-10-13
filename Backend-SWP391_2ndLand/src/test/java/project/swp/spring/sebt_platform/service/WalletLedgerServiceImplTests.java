package project.swp.spring.sebt_platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletStatus;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.impl.WalletLedgerServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletLedgerServiceImplTests {

    WalletRepository walletRepository;
    WalletTransactionRepository walletTransactionRepository;
    WalletLedgerService ledgerService;

    @BeforeEach
    void setup() {
        walletRepository = Mockito.mock(WalletRepository.class);
        walletTransactionRepository = Mockito.mock(WalletTransactionRepository.class);
        ledgerService = new WalletLedgerServiceImpl(walletRepository, walletTransactionRepository);
    }

    @Test
    void createPendingTopUp_createsTransaction() {
        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        when(walletRepository.findByUserId(1L)).thenReturn(wallet);
        when(walletTransactionRepository.findByOrderId(any())).thenReturn(null);
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WalletTransactionEntity tx = ledgerService.createPendingTopUp(1L, "ORDER123", BigDecimal.valueOf(100_000));
        assertNotNull(tx);
        assertEquals("ORDER123", tx.getOrderId());
        assertEquals(0, tx.getAmount().compareTo(BigDecimal.valueOf(100_000)));
        assertEquals(tx.getBalanceBefore(), tx.getBalanceAfter()); // pending keeps same
    }

    @Test
    void completeTopUp_success_incrementsBalanceOnce() {
        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        WalletTransactionEntity tx = WalletTransactionEntity.pendingTopUp("ORDER123", wallet, BigDecimal.valueOf(50_000), 1L);

        when(walletTransactionRepository.findByOrderId("ORDER123"))
                .thenReturn(tx);
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ledgerService.completeTopUp("ORDER123", true, "{}");
        assertEquals(0, wallet.getBalance().compareTo(BigDecimal.valueOf(50_000)));

        // second call should be idempotent (status already COMPLETED)
        ledgerService.completeTopUp("ORDER123", true, "{}");
        assertEquals(0, wallet.getBalance().compareTo(BigDecimal.valueOf(50_000)));
    }
}
