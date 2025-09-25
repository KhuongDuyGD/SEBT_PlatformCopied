package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.TransactionStatus;

@Entity
@Table(name = "transaction_logs",
    indexes = {
        @Index(name = "idx_transaction_logs_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_transaction_logs_created_at", columnList = "created_at")
    }
)
public class TransactionLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Add previous_status if auditing transitions needed.

    // Constructors
    public TransactionLogEntity() {}

    public TransactionLogEntity(TransactionEntity transaction, TransactionStatus status) {
        this.transaction = transaction;
        this.status = status;
    }

    public TransactionLogEntity(TransactionEntity transaction, TransactionStatus status, String notes) {
        this.transaction = transaction;
        this.status = status;
        this.notes = notes;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionEntity transaction) {
        this.transaction = transaction;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
