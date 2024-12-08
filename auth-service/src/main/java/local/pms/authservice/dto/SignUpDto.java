package local.pms.authservice.dto;

public record SignUpDto(
    String username,
    String password,
    String email,
    String firstName,
    String lastName
) {}