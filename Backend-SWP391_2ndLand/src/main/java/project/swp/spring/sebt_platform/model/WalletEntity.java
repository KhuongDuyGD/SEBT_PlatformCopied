package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(cascade = CascadeType.ALL)
    private List<WalletTransactionEntity> transactions;

    @Column(name = "balance", nullable = false, precision = 18, scale = 2,columnDefinition = "DECIMAL(18,2)")
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updated_at;

    public WalletEntity(UserEntity user, BigDecimal balance, WalletStatus status, LocalDateTime updated_at) {
        this.user = user;
        this.balance = balance;
        this.status = status;
        this.updated_at = updated_at;
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

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public List<WalletTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<WalletTransactionEntity> transactions) {
        this.transactions = transactions;
    }

}
