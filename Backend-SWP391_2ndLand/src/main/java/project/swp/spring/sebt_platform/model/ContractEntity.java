package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.swp.spring.sebt_platform.model.enums.ContractStatus;
import project.swp.spring.sebt_platform.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts",
    indexes = {
        @Index(name = "idx_contracts_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_contracts_seller_id", columnList = "seller_id"),
        @Index(name = "idx_contracts_listing_id", columnList = "listing_id"),
        @Index(name = "idx_contracts_status", columnList = "status"),
        @Index(name = "idx_contracts_contract_number", columnList = "contract_number"),
        @Index(name = "idx_contracts_listing_status", columnList = "listing_id, status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_contracts_contract_number", columnNames = "contract_number")
    }
)
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserEntity buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(name = "contract_number", length = 50, nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String contractNumber;

    @Column(name = "handover_datetime", columnDefinition = "DATETIME2")
    private LocalDateTime handoverDatetime;

    @Column(name = "handover_location", length = 600, columnDefinition = "NVARCHAR(600)")
    private String handoverLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_amount", nullable = false, precision = 18, scale = 2, columnDefinition = "DECIMAL(18,2)")
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "buyer_signed", columnDefinition = "BIT DEFAULT 0")
    private Boolean buyerSigned = false;

    @Column(name = "seller_signed", columnDefinition = "BIT DEFAULT 0")
    private Boolean sellerSigned = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    // Remove OneToMany relationships to avoid deep nesting
    // Use repository queries to fetch signatures when needed

    // Constructors
    public ContractEntity() {}

    public ContractEntity(UserEntity buyer, UserEntity seller, ListingEntity listing,
                         String contractNumber, PaymentMethod paymentMethod, BigDecimal paymentAmount) {
        this.buyer = buyer;
        this.seller = seller;
        this.listing = listing;
        this.contractNumber = contractNumber;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getBuyer() {
        return buyer;
    }

    public void setBuyer(UserEntity buyer) {
        this.buyer = buyer;
    }

    public UserEntity getSeller() {
        return seller;
    }

    public void setSeller(UserEntity seller) {
        this.seller = seller;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public LocalDateTime getHandoverDatetime() {
        return handoverDatetime;
    }

    public void setHandoverDatetime(LocalDateTime handoverDatetime) {
        this.handoverDatetime = handoverDatetime;
    }

    public String getHandoverLocation() {
        return handoverLocation;
    }

    public void setHandoverLocation(String handoverLocation) {
        this.handoverLocation = handoverLocation;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public Boolean getBuyerSigned() {
        return buyerSigned;
    }

    public void setBuyerSigned(Boolean buyerSigned) {
        this.buyerSigned = buyerSigned;
    }

    public Boolean getSellerSigned() {
        return sellerSigned;
    }

    public void setSellerSigned(Boolean sellerSigned) {
        this.sellerSigned = sellerSigned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isFullySigned() {
        return Boolean.TRUE.equals(buyerSigned) && Boolean.TRUE.equals(sellerSigned);
    }

    public boolean canBeExecuted() {
        return isFullySigned() && status == ContractStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractEntity)) return false;
        ContractEntity that = (ContractEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
