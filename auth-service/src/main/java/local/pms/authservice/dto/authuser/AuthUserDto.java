package local.pms.authservice.dto.authuser;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record AuthUserDto(
        @NotNull(message = "User identifier is required")
        UUID id,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 255, message = "Password must be between 3 and 255 characters")
        String password,

        @NotNull(message = "Authentication roles are required")
        List<AuthRoleDto> authRoles,

        @NotNull(message = "Authentication permissions are required")
        List<AuthPermissionDto> authPermissions
) {}