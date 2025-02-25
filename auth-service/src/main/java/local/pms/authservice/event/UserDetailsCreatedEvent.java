package local.pms.authservice.event;

import local.pms.authservice.dto.authuser.UserDetailsDto;

public record UserDetailsCreatedEvent(UserDetailsDto userDetailsDto) {}