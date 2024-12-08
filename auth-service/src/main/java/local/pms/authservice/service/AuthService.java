package local.pms.authservice.service;

import local.pms.authservice.dto.SignUpDto;
import local.pms.authservice.dto.authuser.AuthUserDto;

import java.util.UUID;
import java.util.Optional;

public interface AuthService {
    void signUp(SignUpDto signUpDto);
    Optional<AuthUserDto> findByUsername(String username);
    String generateToken(AuthUserDto authUserDto);
    void deleteById(UUID id);
}