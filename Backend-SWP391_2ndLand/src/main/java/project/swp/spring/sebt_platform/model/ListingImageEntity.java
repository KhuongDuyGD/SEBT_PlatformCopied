package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_images",
    indexes = {
        @Index(name = "idx_listing_images_listing_id", columnList = "listing_id"),
        @Index(name = "idx_listing_images_display_order", columnList = "display_order"),
        @Index(name = "uk_listing_images_listing_display_order", columnList = "listing_id, display_order", unique = true)
    }
)
public class ListingImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: If you rely on mainImage in listings, ensure sync or drop mainImage and mark one image as primary.

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
}
