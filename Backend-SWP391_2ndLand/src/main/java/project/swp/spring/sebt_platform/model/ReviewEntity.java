package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
    indexes = {
        @Index(name = "idx_reviews_reviewer_id", columnList = "reviewer_id"),
        @Index(name = "idx_reviews_reviewed_user_id", columnList = "reviewed_user_id")
    }
)
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserEntity reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private UserEntity reviewedUser;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 rating as per ERD note

    @Column(name = "review_text", columnDefinition = "NVARCHAR(MAX)")
    private String reviewText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    // Constructors
    public ReviewEntity() {}

    public ReviewEntity(UserEntity reviewer, UserEntity reviewedUser, Integer rating) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.rating = rating;
    }

    public ReviewEntity(UserEntity reviewer, UserEntity reviewedUser, Integer rating, String reviewText) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserEntity reviewer) {
        this.reviewer = reviewer;
    }

    public UserEntity getReviewedUser() {
        return reviewedUser;
    }

    public void setReviewedUser(UserEntity reviewedUser) {
        this.reviewedUser = reviewedUser;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
