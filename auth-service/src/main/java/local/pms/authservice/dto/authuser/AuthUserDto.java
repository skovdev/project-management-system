package local.pms.authservice.dto.authuser;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;
import java.util.List;

public record AuthUserDto(
        @NotBlank(message = "User identifier is required")
        @Size(min = 36, max = 36, message = "User identifier must be 36 characters")
        UUID id,
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 255, message = "Password must be between 3 and 255 characters")
        String password,
        @NotBlank(message = "Authentication roles is required")
        List<AuthRoleDto> authRoles,
        @NotBlank(message = "Authentication permissions is required")
        List<AuthPermissionDto> authPermissions
) {}