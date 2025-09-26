package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteEntity.FavoriteId.class)
public class FavoriteEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "listing_id")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private ListingEntity listing;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public FavoriteEntity() {}

    public FavoriteEntity(Long userId, Long listingId) {
        this.userId = userId;
        this.listingId = listingId;
    }

    public FavoriteEntity(UserEntity user, ListingEntity listing) {
        this.user = user;
        this.listing = listing;
        this.userId = user.getId();
        this.listingId = listing.getId();
    }

    // Composite Primary Key Class
    public static class FavoriteId implements Serializable {
        private Long userId;
        private Long listingId;

        public FavoriteId() {}

        public FavoriteId(Long userId, Long listingId) {
            this.userId = userId;
            this.listingId = listingId;
        }

        // Getters and setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getListingId() {
            return listingId;
        }

        public void setListingId(Long listingId) {
            this.listingId = listingId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FavoriteId that = (FavoriteId) o;
            return userId.equals(that.userId) && listingId.equals(that.listingId);
        }

        @Override
        public int hashCode() {
            return userId.hashCode() + listingId.hashCode();
        }
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
        if (listing != null) {
            this.listingId = listing.getId();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
