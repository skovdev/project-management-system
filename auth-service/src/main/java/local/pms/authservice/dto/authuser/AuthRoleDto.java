package local.pms.authservice.dto.authuser;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AuthRoleDto(
        @NotBlank(message = "Role identifier is required")
        @Size(min = 36, max = 36, message = "Role id must be 36 characters")
        UUID id,
        @NotBlank(message = "Authority is required")
        @Size(min = 3, max = 255, message = "Authority must be between 3 and 255 characters")
        String authority,
        @NotBlank(message = "Authentication user identifier is required")
        @Size(min = 3, max = 255, message = "Authentication user identifier must be between 3 and 255 characters")
        UUID authUserId) {}
