package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
    indexes = {
        @Index(name = "idx_reviews_reviewer_id", columnList = "reviewer_id"),
        @Index(name = "idx_reviews_reviewed_user_id", columnList = "reviewed_user_id"),
        @Index(name = "idx_reviews_transaction_id", columnList = "transaction_id")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(nullable = false)
    private Integer rating; // Add CHECK 1..5 at DB level

    @Column(columnDefinition = "TEXT")
    private String reviewText;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Add UNIQUE (reviewer_id, transaction_id) if only one review per transaction. Enforce reviewer_id <> reviewed_user_id.

    // Constructors
    public ReviewEntity() {}

    public ReviewEntity(UserEntity reviewer, UserEntity reviewedUser, Integer rating) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.rating = rating;
    }

    public ReviewEntity(UserEntity reviewer, UserEntity reviewedUser, TransactionEntity transaction,
                       Integer rating, String reviewText) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        this.transaction = transaction;
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

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionEntity transaction) {
        this.transaction = transaction;
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
}
