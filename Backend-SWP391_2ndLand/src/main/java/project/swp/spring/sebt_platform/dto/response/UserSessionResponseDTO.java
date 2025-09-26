package project.swp.spring.sebt_platform.model.dto.response;

import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;

public record UserSessionResponseDTO(
    Long id,
    String username,
    String email,
    UserRole role,
    UserStatus status,
    String sessionId
) {}