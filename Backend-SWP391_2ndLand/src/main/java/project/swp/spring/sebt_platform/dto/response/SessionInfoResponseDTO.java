package project.swp.spring.sebt_platform.dto.response;

public class SessionInfoResponseDTO {
    private String sessionId;
    private Long userId;
    private String username;
    private String email;
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public SessionInfoResponseDTO(String sessionId, Long userId, String username, String email,
                                  long creationTime, long lastAccessedTime, int maxInactiveInterval) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public long getCreationTime() { return creationTime; }
    public long getLastAccessedTime() { return lastAccessedTime; }
    public int getMaxInactiveInterval() { return maxInactiveInterval; }
}
