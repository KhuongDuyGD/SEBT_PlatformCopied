package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.PinStatus;
import project.swp.spring.sebt_platform.model.enums.VerifyType;

import java.time.LocalDateTime;

@Entity
@Table(name = "verify_pins",
    indexes = {
        @Index(name = "idx_verify_pins_user_id", columnList = "user_id"),
        @Index(name = "idx_verify_pins_status", columnList = "status"),
        @Index(name = "idx_verify_pins_expired_at", columnList = "expired_at"),
        @Index(name = "idx_verify_pins_user_status", columnList = "user_id, status"),
        @Index(name = "idx_verify_pins_type_status", columnList = "pin_type, status")
    }
)
public class VerifyPinsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "pin", length = 50, nullable = false) // Increased length for hashed values
    private String pin; // This will store the hashed PIN value

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PinStatus status = PinStatus.NOT_EXPIRED;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_type", length = 20)
    private VerifyType pinType;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public VerifyPinsEntity() {}

    public VerifyPinsEntity(UserEntity user, String pin, LocalDateTime expiredAt) {
        this.user = user;
        this.pin = pin;
        this.expiredAt = expiredAt;
    }

    public VerifyPinsEntity(UserEntity user, String pin, VerifyType pinType, LocalDateTime expiredAt) {
        this.user = user;
        this.pin = pin;
        this.pinType = pinType;
        this.expiredAt = expiredAt;
    }

    public VerifyPinsEntity(UserEntity user, String pin, VerifyType pinType, LocalDateTime expiredAt, PinStatus status) {
        this.user = user;
        this.pin = pin;
        this.pinType = pinType;
        this.expiredAt = expiredAt;
        this.status = status;
    }

    // Utility methods for business logic
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public boolean isActive() {
        return this.status == PinStatus.NOT_EXPIRED && !isExpired();
    }

    public void markAsUsed() {
        this.status = PinStatus.USED;
    }

    public void markAsExpired() {
        this.status = PinStatus.EXPIRED;
    }

    // Check if PIN matches (this method expects hashed input for comparison)
    public boolean matches(String hashedInputPin) {
        return this.pin != null && this.pin.equals(hashedInputPin);
    }

    // Check if PIN is valid for verification
    public boolean isValidForVerification(String hashedInputPin) {
        return isActive() && matches(hashedInputPin);
    }

    // Get remaining time before expiry
    public long getMinutesUntilExpiry() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.expiredAt).toMinutes();
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

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public PinStatus getStatus() {
        return status;
    }

    public void setStatus(PinStatus status) {
        this.status = status;
    }

    public VerifyType getPinType() {
        return pinType;
    }

    public void setPinType(VerifyType pinType) {
        this.pinType = pinType;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
