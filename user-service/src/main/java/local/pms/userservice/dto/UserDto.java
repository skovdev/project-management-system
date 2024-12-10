package local.pms.userservice.dto;

import java.util.UUID;

public record UserDto(UUID id, String firstName, String lastName, String email, UUID authUserId) {}
