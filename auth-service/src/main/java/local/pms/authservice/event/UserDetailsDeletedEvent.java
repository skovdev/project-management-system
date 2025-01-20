package local.pms.authservice.event;

import java.util.UUID;

public record UserDetailsDeletedEvent(UUID authUserId) {}