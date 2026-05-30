package local.pms.notificationservice.event;

import java.util.UUID;

public record UserDetailsDto(
        String firstName,
        String lastName,
        String email,
        UUID authUserId
) {}
