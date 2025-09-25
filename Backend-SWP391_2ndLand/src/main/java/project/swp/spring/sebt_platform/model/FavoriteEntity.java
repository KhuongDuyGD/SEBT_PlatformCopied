package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites",
    indexes = {
        @Index(name = "uk_favorites_user_listing", columnList = "user_id, listing_id", unique = true),
        @Index(name = "idx_favorites_listing_id", columnList = "listing_id")
    }
)
public class FavoriteEntity {
    @EmbeddedId
    private FavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listingId")
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // EmbeddedId class for composite key
    @Embeddable
    public static class FavoriteId implements java.io.Serializable {
        private Long userId;
        private Long listingId;
        // equals & hashCode ...
    }
    // Getters and setters ...
}

