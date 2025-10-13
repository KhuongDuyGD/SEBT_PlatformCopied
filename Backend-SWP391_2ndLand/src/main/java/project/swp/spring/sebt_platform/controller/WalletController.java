package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;
import project.swp.spring.sebt_platform.repository.WalletRepository;
import project.swp.spring.sebt_platform.repository.WalletTransactionRepository;
import project.swp.spring.sebt_platform.service.VnpayService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final VnpayService vnpayService;

    public WalletController(WalletRepository walletRepository,
                            WalletTransactionRepository walletTransactionRepository,
                            VnpayService vnpayService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.vnpayService = vnpayService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyWallet(HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
        if (userId == null) return ResponseEntity.status(401).body("login required");
        WalletEntity wallet = walletRepository.findByUserId(userId);
        if (wallet == null) return ResponseEntity.status(404).body("wallet not found");
        return ResponseEntity.ok(new WalletBalanceDTO(wallet.getBalance()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> listTransactions(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) WalletPurpose purpose) {
        Long userId = getUserIdFromSession(request);
        if (userId == null) return ResponseEntity.status(401).body("login required");
        PageRequest pr = PageRequest.of(page, size);
        Page<WalletTransactionEntity> txPage;
        if (purpose != null) {
            txPage = walletTransactionRepository.findByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose, pr);
        } else {
            txPage = walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pr);
        }
        return ResponseEntity.ok(txPage.map(WalletTransactionDTO::from));
    }

    @PostMapping("/topups")
    public ResponseEntity<?> createTopUp(@RequestBody TopUpRequest requestBody, HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
        if (userId == null) return ResponseEntity.status(401).body("login required");
        if (requestBody == null || requestBody.amount() == null || requestBody.amount() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("amount must be positive");
        }
        try {
            VnpayService.TopUpIntent intent = vnpayService.createTopUpIntent(requestBody.amount(), userId, request);
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

    public record TopUpRequest(Double amount) {}
    public record TopUpIntentResponse(String orderId, String paymentUrl, Double amount, java.time.OffsetDateTime expiresAt) {}

    @GetMapping("/topups/{orderId}")
    public ResponseEntity<?> getTopUpStatus(@PathVariable String orderId, HttpServletRequest request) {
        Long userId = getUserIdFromSession(request);
        if (userId == null) return ResponseEntity.status(401).body("login required");
        WalletTransactionEntity tx = walletTransactionRepository.findByOrderId(orderId);
        if (tx == null || !userId.equals(tx.getUserId())) return ResponseEntity.status(404).body("not found");
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
}
