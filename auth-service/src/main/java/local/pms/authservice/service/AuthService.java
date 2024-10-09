package local.pms.authservice.service;

import local.pms.authservice.dto.authuser.AuthUserDto;

import java.util.Optional;

public interface AuthService {
    Optional<AuthUserDto> findByUsername(String username);
    String generateToken(AuthUserDto authUserDto);
}