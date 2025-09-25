package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Column(length = 50, nullable = false, unique = true)
    private String transactionCode;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(length = 50)
    private String paymentGateway;

    @Column(length = 100)
    private String gatewayTransactionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    // Note: Enum mismatch: status default pending but enum lacks pending; fix either enum or default. Enforce gatewayTransactionId NOT NULL when paymentGateway IS NOT NULL.
    // Getters and setters ...
}

