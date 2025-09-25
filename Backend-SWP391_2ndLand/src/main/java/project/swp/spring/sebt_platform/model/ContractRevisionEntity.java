package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_revisions",
    indexes = {
        @Index(name = "idx_contract_revisions_contract_id", columnList = "contract_id"),
        @Index(name = "idx_contract_revisions_edited_by", columnList = "edited_by")
    }
)
public class ContractRevisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private UserEntity editedBy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String changesMade; // Suggest JSON structured format

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime editedAt;

    // Note: Add revision_number column if strict ordering required.
    // Getters and setters ...
}

