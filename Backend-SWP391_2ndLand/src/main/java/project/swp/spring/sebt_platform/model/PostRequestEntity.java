package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;

@Entity
@Table(name = "post_requests",
    indexes = {
        @Index(name = "idx_post_requests_status", columnList = "status"),
        @Index(name = "idx_post_requests_requested_date", columnList = "requested_date")
    }
)
public class PostRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @Column(nullable = false)
    private java.time.LocalDate requestedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime reviewedAt;

    // Note: If multiple revisions allowed: consider versioning or unique active per listing.
    // Getters and setters ...
}

