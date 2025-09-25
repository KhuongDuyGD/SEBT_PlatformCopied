package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;

public record UserSessionDTO(
    Long id,
    String username,
    String email,
    UserRole role,
    UserStatus status,
    String sessionId
) {}