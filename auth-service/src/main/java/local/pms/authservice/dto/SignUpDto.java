package local.pms.authservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 255, message = "Password must be between 3 and 255 characters")
        String password,
        @NotBlank(message = "Email is required")
        @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
        @Email(message = "Email must be a valid email address")
        String email,
        @NotBlank(message = "First name is required")
        @Size(min = 3, max = 255, message = "First name must be between 3 and 255 characters")
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(min = 3, max = 255, message = "Last name must be between 3 and 255 characters")
        String lastName
) {}