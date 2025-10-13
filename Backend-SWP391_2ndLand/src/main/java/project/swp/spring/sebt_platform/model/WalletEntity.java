package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.swp.spring.sebt_platform.model.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallets")
public class WalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    // Bidirectional mapping; WalletTransactionEntity owns the FK wallet_id
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<WalletTransactionEntity> transactions;

    @Column(name = "balance", nullable = false, precision = 18, scale = 2, columnDefinition = "DECIMAL(18,2)")
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    @Column(name = "version")
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    public WalletEntity(UserEntity user, BigDecimal balance, WalletStatus status, LocalDateTime updatedAt) {
        this.user = user;
        this.balance = balance;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public WalletEntity() {
    }


    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public List<WalletTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<WalletTransactionEntity> transactions) {
        this.transactions = transactions;
    }

}
