package local.pms.authservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record SignInDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 255, message = "Password must be between 3 and 255 characters")
        String password) {}