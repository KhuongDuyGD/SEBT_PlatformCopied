package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;

@Entity
@Table(name = "listings",
    indexes = {
        @Index(name = "idx_listings_seller_id", columnList = "seller_id"),
        @Index(name = "idx_listings_status", columnList = "status"),
        @Index(name = "idx_listings_listing_type", columnList = "listing_type"),
        @Index(name = "idx_listings_location", columnList = "location"),
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String mainImage;

    @Column(length = 200, nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingType listingType = ListingType.NORMAL;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column
    private Integer viewsCount = 0;

    @Column
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Note: Enforce workflow externally. For full-text search add separate search index (outside DBML).
    // Getters and setters ...
}

