package local.pms.userservice.event;

import java.util.UUID;

public record UserDetailsDeletedEvent(UUID authUserId) {}