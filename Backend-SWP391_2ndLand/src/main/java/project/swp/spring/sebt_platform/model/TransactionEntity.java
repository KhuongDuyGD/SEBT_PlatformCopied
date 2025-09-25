package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions",
    indexes = {
        @Index(name = "idx_transactions_status", columnList = "status"),
        @Index(name = "idx_transactions_transaction_code", columnList = "transaction_code"),
        @Index(name = "idx_transactions_created_at", columnList = "created_at"),
        @Index(name = "idx_transactions_contract_id", columnList = "contract_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_transactions_transaction_code", columnNames = "transaction_code")
    }
)
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Column(name = "transaction_code", length = 50, nullable = false, unique = true)
    private String transactionCode;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.DEALING; // Default to DEALING as per ERD note about enum mismatch

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionLogEntity> transactionLogs;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews;

    // Constructors
    public TransactionEntity() {}

    public TransactionEntity(ContractEntity contract, String transactionCode, BigDecimal amount) {
        this.contract = contract;
        this.transactionCode = transactionCode;
        this.amount = amount;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<TransactionLogEntity> getTransactionLogs() {
        return transactionLogs;
    }

    public void setTransactionLogs(List<TransactionLogEntity> transactionLogs) {
        this.transactionLogs = transactionLogs;
    }

    public List<ReviewEntity> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewEntity> reviews) {
        this.reviews = reviews;
    }
}
