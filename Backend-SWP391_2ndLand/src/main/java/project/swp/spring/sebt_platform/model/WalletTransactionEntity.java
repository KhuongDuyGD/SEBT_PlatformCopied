package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;
import project.swp.spring.sebt_platform.model.enums.WalletEntryType;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_transactions_wallet_id", columnList = "wallet_id"),
    @Index(name = "uk_wallet_transactions_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_wallet_transactions_status", columnList = "status"),
    @Index(name = "idx_wallet_transactions_purpose", columnList = "purpose"),
    @Index(name = "idx_wallet_transactions_user", columnList = "user_id"),
    @Index(name = "idx_wallet_transactions_created_at", columnList = "created_at")})
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", length = 64, nullable = false, unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "NVARCHAR(20)")
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 40, nullable = false)
    private WalletPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 20, nullable = false)
    private WalletEntryType entryType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "listing_id")
    private Long listingId;

    @Column(name = "external_ref", length = 100)
    private String externalRef;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "metadata", columnDefinition = "NVARCHAR(MAX)")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    public WalletTransactionEntity() {}

    public static WalletTransactionEntity pendingTopUp(String orderId, WalletEntity wallet, BigDecimal amount, Long userId) {
        WalletTransactionEntity tx = new WalletTransactionEntity();
        tx.orderId = orderId;
        tx.wallet = wallet;
        tx.amount = amount;
        tx.balanceBefore = wallet.getBalance();
        tx.balanceAfter = wallet.getBalance(); // will update on completion
        tx.status = TransactionStatus.PENDING;
        tx.purpose = WalletPurpose.TOP_UP;
        tx.entryType = WalletEntryType.CREDIT;
        tx.userId = userId;
        tx.description = "VNPay top-up initiated";
        return tx;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getId() {
        return id;
    }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public WalletPurpose getPurpose() { return purpose; }
    public void setPurpose(WalletPurpose purpose) { this.purpose = purpose; }
    public WalletEntryType getEntryType() { return entryType; }
    public void setEntryType(WalletEntryType entryType) { this.entryType = entryType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public WalletEntity getWallet() { return wallet; }
    public void setWallet(WalletEntity wallet) { this.wallet = wallet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
