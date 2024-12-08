package local.pms.authservice.event;

import local.pms.authservice.dto.SignUpDto;

import java.util.UUID;

public record UserDetailsCreatedEvent(SignUpDto signUp, UUID authUserId) {}