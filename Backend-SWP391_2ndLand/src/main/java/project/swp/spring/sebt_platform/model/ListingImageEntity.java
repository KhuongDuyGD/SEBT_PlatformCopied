package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "listing_images",
    indexes = {
        @Index(name = "idx_listing_images_listing_id", columnList = "listing_id"),
    }
)
public class ListingImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    // Mapping for display order (DB seems to enforce unique (listing_id, display_order) via index 'uk_listing_images_listing_display')
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "public_id", columnDefinition = "VARCHAR(255)")
    private String publicId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ListingImageEntity() {}

    public ListingImageEntity(ListingEntity listing, String imageUrl) {
        this.listing = listing;
        this.imageUrl = imageUrl;
    }

    public ListingImageEntity(ListingEntity listing, String imageUrl, String publicId) {
        this.listing = listing;
        this.imageUrl = imageUrl;
        this.publicId = publicId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // Alias methods for compatibility với code hiện tại
    public String getUrl() {
        return imageUrl;
    }

    public void setUrl(String url) {
        this.imageUrl = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListingImageEntity)) return false;
        ListingImageEntity that = (ListingImageEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
