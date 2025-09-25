package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import project.swp.spring.sebt_platform.model.enums.UserStatus;
import project.swp.spring.sebt_platform.model.enums.UserRole;

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

    @Column(name = "username", length = 50, nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
    private String username;

    @Column(name = "password", length = 255, nullable = false, columnDefinition = "VARCHAR(255)")
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(name = "phone_number", length = 15, columnDefinition = "VARCHAR(15)")
    private String phoneNumber;

    @Column(name = "avatar", columnDefinition = "NVARCHAR(MAX)")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private UserRole role = UserRole.MEMBER;

    @Column(name = "personal_pins", columnDefinition = "NVARCHAR(MAX)")
    private String personalPins;

    @Column(name = "salt", length = 32, columnDefinition = "VARCHAR(32)")
    private String salt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME2")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME2")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingEntity> listings;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContractEntity> buyerContracts;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContractEntity> sellerContracts;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewEntity> givenReviews;

    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewEntity> receivedReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FavoriteEntity> favorites;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComplaintEntity> complaints;

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
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPersonalPins() {
        return personalPins;
    }

    public void setPersonalPins(String personalPins) {
        this.personalPins = personalPins;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ListingEntity> getListings() {
        return listings;
    }

    public void setListings(List<ListingEntity> listings) {
        this.listings = listings;
    }

    public List<ContractEntity> getBuyerContracts() {
        return buyerContracts;
    }

    public void setBuyerContracts(List<ContractEntity> buyerContracts) {
        this.buyerContracts = buyerContracts;
    }

    public List<ContractEntity> getSellerContracts() {
        return sellerContracts;
    }

    public void setSellerContracts(List<ContractEntity> sellerContracts) {
        this.sellerContracts = sellerContracts;
    }

    public List<ReviewEntity> getGivenReviews() {
        return givenReviews;
    }

    public void setGivenReviews(List<ReviewEntity> givenReviews) {
        this.givenReviews = givenReviews;
    }

    public List<ReviewEntity> getReceivedReviews() {
        return receivedReviews;
    }

    public void setReceivedReviews(List<ReviewEntity> receivedReviews) {
        this.receivedReviews = receivedReviews;
    }

    public List<FavoriteEntity> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoriteEntity> favorites) {
        this.favorites = favorites;
    }

    public List<ComplaintEntity> getComplaints() {
        return complaints;
    }

    public void setComplaints(List<ComplaintEntity> complaints) {
        this.complaints = complaints;
    }
}
