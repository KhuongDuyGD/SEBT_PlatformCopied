package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "listing_images",
    indexes = {
        @Index(name = "idx_listing_images_listing_id", columnList = "listing_id"),
        @Index(name = "idx_listing_images_display_order", columnList = "display_order")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_listing_images_listing_display", columnNames = {"listing_id", "display_order"})
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

    @Column(name = "display_order", columnDefinition = "int DEFAULT 0")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ListingImageEntity() {}

    public ListingImageEntity(ListingEntity listing, String imageUrl, Integer displayOrder) {
        this.listing = listing;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
