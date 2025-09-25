package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import project.swp.spring.sebt_platform.model.enums.SignatureMethod;

@Entity
@Table(name = "contract_signatures",
    indexes = {
        @Index(name = "idx_contract_signatures_contract_id", columnList = "contract_id"),
        @Index(name = "idx_contract_signatures_user_id", columnList = "user_id"),
        @Index(name = "uk_contract_signatures_contract_user", columnList = "contract_id, user_id", unique = true)
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
    @Column(nullable = false, updatable = false)
    private LocalDateTime signedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignatureMethod signatureMethod;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    // Note: Unique to prevent duplicate signatures from same user.

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
