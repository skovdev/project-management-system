package local.pms.userservice.event;

import local.pms.userservice.dto.UserDetailsDto;

public record UserDetailsCreatedEvent(UserDetailsDto userDetailsDto) {}