package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import project.swp.spring.sebt_platform.model.enums.UserStatus;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.VerifyType;

@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column
    private String address;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.BLOCKED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.MEMBER;

    @Column(columnDefinition = "TEXT")
    private String personalPins;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerifyPinsEntity> verifyPins;

    @Column(length = 32)
    private String salt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // TODO: Consider adding failedLoginAttempts, lockedUntil, passwordChangedAt in future.

    // Constructors
    public UserEntity() {}

    public UserEntity(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public UserEntity(String username, String password, String email, String salt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.salt = salt;
    }

    // Getters and setters
    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPersonalPins() {
        return personalPins;
    }

    public List<VerifyPinsEntity> getVerifyPins() {
        return verifyPins;
    }

    // Add utility method to get latest active pin
    public VerifyPinsEntity getLatestVerifyPin() {
        if (verifyPins == null || verifyPins.isEmpty()) {
            return null;
        }
        return verifyPins.get(verifyPins.size() - 1);
    }

    // Add utility method to get active pins by type
    public List<VerifyPinsEntity> getActivePinsByType(VerifyType pinType) {
        if (verifyPins == null) {
            return new java.util.ArrayList<>();
        }
        return verifyPins.stream()
            .filter(pin -> pin.isActive() &&
                          (pinType == null || pinType.equals(pin.getPinType())))
            .collect(java.util.stream.Collectors.toList());
    }

    public String getSalt() {
        return salt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setPersonalPins(String personalPins) {
        this.personalPins = personalPins;
    }

    public void setVerifyPins(List<VerifyPinsEntity> verifyPins) {
        this.verifyPins = verifyPins;
    }

    // Add utility method to add a single pin
    public void addVerifyPin(VerifyPinsEntity verifyPin) {
        if (this.verifyPins == null) {
            this.verifyPins = new java.util.ArrayList<>();
        }
        this.verifyPins.add(verifyPin);
        verifyPin.setUser(this); // Set bidirectional relationship
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
