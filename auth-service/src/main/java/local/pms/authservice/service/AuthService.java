package local.pms.authservice.service;

import local.pms.authservice.dto.SignUpDto;

import local.pms.authservice.dto.authuser.AuthUserDto;

import java.util.UUID;
import java.util.Optional;

public interface AuthService {
    Optional<AuthUserDto> findById(UUID id);
    Optional<AuthUserDto> findByUsername(String username);
    void signUp(SignUpDto signUpDto);
    AuthUserDto authenticate(String username, String password);
    String generateToken(AuthUserDto authUserDto);
    void deleteById(UUID id);
}