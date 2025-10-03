package project.swp.spring.sebt_platform.dto.response;

import java.time.LocalDateTime;

public class UserProfileResponseDTO {
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private LocalDateTime createdAt;

    // Constructors
    public UserProfileResponseDTO() {}

    public UserProfileResponseDTO(String username, String email, String phoneNumber, String avatarUrl, LocalDateTime createdAt) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}