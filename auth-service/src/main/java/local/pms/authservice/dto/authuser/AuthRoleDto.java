package local.pms.authservice.dto.authuser;

import java.util.UUID;

public record AuthRoleDto(UUID id, String authority, UUID authUserId) {}