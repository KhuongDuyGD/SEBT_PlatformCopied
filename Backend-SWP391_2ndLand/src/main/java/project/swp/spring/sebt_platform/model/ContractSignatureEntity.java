package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.SignatureMethod;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_signatures",
    indexes = {
        @Index(name = "idx_contract_signatures_contract_id", columnList = "contract_id"),
        @Index(name = "idx_contract_signatures_user_id", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_contract_signatures_contract_user", columnNames = {"contract_id", "user_id"})
    }
)
public class ContractSignatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "signed_at", nullable = false, updatable = false)
    private LocalDateTime signedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_method", nullable = false, length = 20)
    private SignatureMethod signatureMethod;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Constructors
    public ContractSignatureEntity() {}

    public ContractSignatureEntity(ContractEntity contract, UserEntity user, SignatureMethod signatureMethod) {
        this.contract = contract;
        this.user = user;
        this.signatureMethod = signatureMethod;
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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public SignatureMethod getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(SignatureMethod signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
