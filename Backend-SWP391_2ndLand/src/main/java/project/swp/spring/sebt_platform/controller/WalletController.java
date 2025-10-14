package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;
import project.swp.spring.sebt_platform.service.VnpayService;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.exception.AuthRequiredException;
import project.swp.spring.sebt_platform.exception.NotFoundException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@Validated
public class WalletController {

    private final VnpayService vnpayService;
    private final WalletLedgerService walletLedgerService;

    public WalletController(
                            VnpayService vnpayService,
                            WalletLedgerService walletLedgerService) {
        this.vnpayService = vnpayService;
        this.walletLedgerService = walletLedgerService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyWallet(HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
    if (userId == null) throw new AuthRequiredException();
    WalletEntity wallet = walletLedgerService.getWalletByUserId(userId);
    if (wallet == null) throw new NotFoundException("wallet", userId);
    return ResponseEntity.ok(new WalletBalanceDTO(wallet.getBalance()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> listTransactions(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) WalletPurpose purpose) {
        Long userId = getUserIdFromSession(request);
    if (userId == null) throw new AuthRequiredException();
        var pr = PageRequest.of(page, size);
        var txPage = walletLedgerService.getTransactions(userId, purpose, pr);
        return ResponseEntity.ok(txPage.map(WalletTransactionDTO::from));
    }

    @PostMapping("/topups")
    public ResponseEntity<?> createTopUp(@RequestBody TopUpRequest requestBody, HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
    if (userId == null) throw new AuthRequiredException();
        if (requestBody == null || requestBody.amount() == null || requestBody.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        try {
            VnpayService.TopUpIntent intent = vnpayService.createTopUpIntent(requestBody.amount().doubleValue(), userId, request);
            TopUpIntentResponse resp = new TopUpIntentResponse(intent.orderId(), intent.paymentUrl(), intent.amount(), intent.expiresAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("failed to create top-up intent");
        }
    }

    private Long getUserIdFromSession(HttpServletRequest request) {
        return (Long) (request.getSession(false) != null ? request.getSession(false).getAttribute("userId") : null);
    }

    public record WalletBalanceDTO(java.math.BigDecimal balance) {}

    public record WalletTransactionDTO(String orderId,
                                       java.math.BigDecimal amount,
                                       java.math.BigDecimal balanceBefore,
                                       java.math.BigDecimal balanceAfter,
                                       String status,
                                       String purpose,
                                       String entryType,
                                       java.time.LocalDateTime createdAt,
                                       String description) {
        static WalletTransactionDTO from(WalletTransactionEntity e) {
            return new WalletTransactionDTO(
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

    public record TopUpRequest(BigDecimal amount) {}
    public record TopUpIntentResponse(String orderId, String paymentUrl, Double amount, java.time.OffsetDateTime expiresAt) {}

    @GetMapping("/topups/{orderId}")
    public ResponseEntity<?> getTopUpStatus(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
    if (userId == null) throw new AuthRequiredException();
        WalletTransactionEntity tx = walletLedgerService.getTransactionByOrderId(orderId);
        if (tx == null || !userId.equals(tx.getUserId())) throw new NotFoundException("transaction", orderId);
        return ResponseEntity.ok(new WalletTransactionDTO(
                tx.getOrderId(),
                tx.getAmount(),
                tx.getBalanceBefore(),
                tx.getBalanceAfter(),
                tx.getStatus().name(),
                tx.getPurpose() != null ? tx.getPurpose().name() : null,
                tx.getEntryType() != null ? tx.getEntryType().name() : null,
                tx.getCreatedAt(),
                tx.getDescription()
        ));
    }

    @PostMapping("/topups/{orderId}/complete")
    public ResponseEntity<?> completeTopUp(@PathVariable String orderId,
                                           @RequestParam(defaultValue = "true") boolean success,
                                           HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
    if (userId == null) throw new AuthRequiredException();
        WalletTransactionEntity tx = walletLedgerService.getTransactionByOrderId(orderId);
        if (tx == null || !userId.equals(tx.getUserId())) throw new NotFoundException("transaction", orderId);
        if (!tx.getStatus().name().equals("PENDING")) {
            return ResponseEntity.ok(WalletTransactionDTO.from(tx));
        }
        // Mark completed (simulate manual success path; amount already on tx)
        walletLedgerService.completeTopUp(orderId, success, "{\"manual\":true}", tx.getAmount());
        WalletTransactionEntity updated = walletLedgerService.getTransactionByOrderId(orderId);
        return ResponseEntity.ok(WalletTransactionDTO.from(updated));
    }
}
