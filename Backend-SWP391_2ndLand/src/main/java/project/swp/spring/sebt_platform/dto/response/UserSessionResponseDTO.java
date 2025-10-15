package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;

public class UserSessionResponseDTO {
    private Long id;
    private String username;
    private String email;
    private double balance;
    private UserRole role;
    private UserStatus status;
    private String sessionId;

    public UserSessionResponseDTO() {
    }

    public UserSessionResponseDTO(Long id, String username, String email,double balance, UserRole role, UserStatus status, String sessionId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;
        this.role = role;
        this.status = status;
        this.sessionId = sessionId;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
