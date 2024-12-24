package local.pms.authservice.dto.authuser;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AuthPermissionDto(
        @NotBlank(message = "Permission identifier is required")
        @Size(min = 36, max = 36, message = "Permission id must be 36 characters")
        UUID id,
        @NotBlank(message = "Permission is required")
        @Size(min = 3, max = 255, message = "Permission must be between 3 and 255 characters")
        String permission,
        @NotBlank(message = "Authentication user identifier is required")
        @Size(min = 3, max = 255, message = "Authentication user identifier must be between 3 and 255 characters")
        UUID authUserId) {}
