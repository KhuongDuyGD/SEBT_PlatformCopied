package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.response.TopUpIntentResponseDTO;
import project.swp.spring.sebt_platform.dto.response.WalletTransactionResponseDTO;
import project.swp.spring.sebt_platform.model.WalletEntity;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;
import project.swp.spring.sebt_platform.service.VnpayService;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.util.Utils;

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

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Wallet balance returned",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = BigDecimal.class))),
                    @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Wallet not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<?> getMyWallet(HttpServletRequest request) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");
            }
            WalletEntity wallet = walletLedgerService.getWalletByUserId(userId);
            if (wallet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy ví");
            }
            return ResponseEntity.ok(wallet.getBalance());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Wallet transactions returned",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = WalletTransactionResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/transactions")
    public ResponseEntity<?> listTransactions(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) WalletPurpose purpose) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");

            var pr = PageRequest.of(page, size);
            var txPage = walletLedgerService.getTransactions(userId, purpose, pr);
            return ResponseEntity.ok(txPage.map(WalletTransactionResponseDTO::from));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Top-up intent created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TopUpIntentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/topups")
    public ResponseEntity<?> createTopUp(@RequestBody double amount, HttpServletRequest request) {
        Long userId = Utils.getUserIdFromSession(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");
        if (amount <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số tiền phải lớn hơn 0");
        try {
            VnpayService.TopUpIntent intent = vnpayService.createTopUpIntent(amount, userId, request);
            TopUpIntentResponseDTO resp = new TopUpIntentResponseDTO(intent.orderId(), intent.paymentUrl(), intent.amount(), intent.expiresAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to create top-up intent");
        }
    }

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Top-up status returned",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = WalletTransactionResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/topups/{orderId}")
    public ResponseEntity<?> getTopUpStatus(@PathVariable String orderId, HttpServletRequest request) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in");
            WalletTransactionEntity tx = walletLedgerService.getTransactionByOrderId(orderId);

            if (tx == null || !userId.equals(tx.getUserId())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order");
            return ResponseEntity.ok(new WalletTransactionResponseDTO(
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }

    }

    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Top-up completed",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = WalletTransactionResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/topups/{orderId}/complete")
    public ResponseEntity<?> completeTopUp(@PathVariable String orderId,
                                           @RequestParam(defaultValue = "true") boolean success,
                                           HttpServletRequest request){
        try {
            Long userId = Utils.getUserIdFromSession(request);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not logged in");
            WalletTransactionEntity tx = walletLedgerService.getTransactionByOrderId(orderId);
            if (tx == null || !userId.equals(tx.getUserId())) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order");
            if (!tx.getStatus().name().equals("PENDING")) {
                return ResponseEntity.ok(WalletTransactionResponseDTO.from(tx));
            }
            // Mark completed (simulate manual success path; amount already on tx)
            walletLedgerService.completeTopUp(orderId, success, "{\"manual\":true}", tx.getAmount());
            WalletTransactionEntity updated = walletLedgerService.getTransactionByOrderId(orderId);
            return ResponseEntity.ok(WalletTransactionResponseDTO.from(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }

    }
}
