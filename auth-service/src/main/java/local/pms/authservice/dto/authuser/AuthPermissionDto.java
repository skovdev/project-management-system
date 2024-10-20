package local.pms.authservice.dto.authuser;

import java.util.UUID;

public record AuthPermissionDto(UUID id, String permission, UUID authUserId) {}
