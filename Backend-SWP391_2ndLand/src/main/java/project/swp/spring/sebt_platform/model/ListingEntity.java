package project.swp.spring.sebt_platform.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;

@Entity
@Table(name = "listings",
    indexes = {
        @Index(name = "idx_listings_seller_id", columnList = "seller_id"),
        @Index(name = "idx_listings_status", columnList = "status"),
        @Index(name = "idx_listings_listing_type", columnList = "listing_type"),
        @Index(name = "idx_listings_created_at", columnList = "created_at"),
        @Index(name = "idx_listings_status_expires_at", columnList = "status, expires_at")
    }
)
public class ListingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserEntity seller;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "title", length = 255, nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String title;

    @Column (name = "thumbnail_public_id", length = 255, columnDefinition = "VARCHAR(255)")
    private String thumbnailPublicId;

    @Column(name = "thumbnail_image", columnDefinition = "NVARCHAR(MAX)")
    private String thumbnailImage;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private ListingStatus status = ListingStatus.SUSPENDED;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private ListingType listingType = ListingType.NORMAL;

    @Column(name = "price", nullable = false, precision = 18, scale = 2, columnDefinition = "DECIMAL(18,2)")
    private BigDecimal price;

    @Column(name = "views_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewsCount = 0;

    @Column(name = "expires_at", columnDefinition = "DATETIME2")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    // Remove OneToMany relationships to avoid deep nesting
    // Use repository queries to fetch related data when needed

    // Constructors
    public ListingEntity() {}

    public ListingEntity(UserEntity seller, ProductEntity product, String title, BigDecimal price) {
        this.seller = seller;
        this.product = product;
        this.title = title;
        this.price = price;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getSeller() {
        return seller;
    }

    public void setSeller(UserEntity seller) {
        this.seller = seller;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public String getThumbnailPublicId() {
        return thumbnailPublicId;
    }

    public void setThumbnailPublicId(String thumbnailPublicId) {
        this.thumbnailPublicId = thumbnailPublicId;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    // Helper method to increment view count
    public void incrementViewCount() {
        this.viewsCount = (this.viewsCount == null ? 0 : this.viewsCount) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListingEntity)) return false;
        ListingEntity that = (ListingEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
