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

    @Column(name = "changes_made", columnDefinition = "TEXT", nullable = false)
    private String changesMade;

    @CreationTimestamp
    @Column(name = "edited_at", nullable = false, updatable = false)
    private LocalDateTime editedAt;

    // Constructors
    public ContractRevisionEntity() {}

    public ContractRevisionEntity(ContractEntity contract, UserEntity editedBy, String changesMade) {
        this.contract = contract;
        this.editedBy = editedBy;
        this.changesMade = changesMade;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

    public UserEntity getEditedBy() {
        return editedBy;
    }

    public void setEditedBy(UserEntity editedBy) {
        this.editedBy = editedBy;
    }

    public String getChangesMade() {
        return changesMade;
    }

    public void setChangesMade(String changesMade) {
        this.changesMade = changesMade;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
}
