package project.swp.spring.sebt_platform.dto.response;

public record RegisterResponseDTO(
    boolean success,
    String message,
    String email
) {}

