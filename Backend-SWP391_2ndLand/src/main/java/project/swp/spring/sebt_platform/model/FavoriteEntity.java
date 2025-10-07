package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites",
    indexes = {
        @Index(name = "idx_favorites_user_id", columnList = "user_id"),
        @Index(name = "idx_favorites_listing_id", columnList = "listing_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_favorites_user_listing", columnNames = {"user_id", "listing_id"})
    }
)
public class FavoriteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    // Constructors
    public FavoriteEntity() {}

    public FavoriteEntity(UserEntity user, ListingEntity listing) {
        this.user = user;
        this.listing = listing;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteEntity)) return false;
        FavoriteEntity that = (FavoriteEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
