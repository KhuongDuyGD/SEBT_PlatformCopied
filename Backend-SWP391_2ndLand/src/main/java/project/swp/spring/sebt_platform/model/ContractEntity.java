package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.ContractStatus;
import project.swp.spring.sebt_platform.model.enums.PaymentMethod;

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

    @Column(length = 50, nullable = false, unique = true)
    private String contractNumber;

    @Column
    private LocalDateTime handoverDatetime;

    @Column(length = 300)
    private String handoverLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column
    private Boolean buyerSigned = false;

    @Column
    private Boolean sellerSigned = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Note: Add CHECK buyer_id <> seller_id. Consider UNIQUE partial: listing_id WHERE status IN (draft,pending_buyer,pending_seller,active).

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
