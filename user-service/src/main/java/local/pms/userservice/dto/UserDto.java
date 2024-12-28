package local.pms.userservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserDto(
        @NotBlank(message = "User identifier is required")
        UUID id,
        @NotBlank(message = "First name is required")
        @Size(min = 3, max = 255, message = "First name must be between 3 and 255 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(min = 3, max = 255, message = "Last name must be between 3 and 255 characters")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,
        @NotBlank(message = "Authentication user identifier is required")
        UUID authUserId) {}
