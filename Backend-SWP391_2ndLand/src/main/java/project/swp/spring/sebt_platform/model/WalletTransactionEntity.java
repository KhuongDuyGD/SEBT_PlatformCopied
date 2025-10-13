package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_transactions_wallet_id", columnList = "wallet_id"),
        @Index(name = "uk_wallet_transactions_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_wallet_transactions_status", columnList = "status"),
        @Index(name = "idx_wallet_transactions_created_at", columnList = "created_at")})
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", length = 50, nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
    private String OrderId;


    @Column(name = "balance", nullable = false, precision = 18, scale = 2,columnDefinition = "DECIMAL(18,2)")
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 18, scale = 2, columnDefinition = "NVARCHAR(50)")
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2, columnDefinition = "NVARCHAR(50)")
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "NVARCHAR(20)")
    private TransactionStatus status;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    public WalletTransactionEntity(
                                   BigDecimal amount,
                                   BigDecimal balanceBefore,
                                   BigDecimal balanceAfter,
                                   String description) {
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public WalletTransactionEntity() {}


    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
