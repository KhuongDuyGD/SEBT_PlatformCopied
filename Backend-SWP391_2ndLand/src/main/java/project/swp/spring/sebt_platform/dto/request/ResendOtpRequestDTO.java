package project.swp.spring.sebt_platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequestDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email
) {}
