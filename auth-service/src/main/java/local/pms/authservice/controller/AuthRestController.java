package local.pms.authservice.controller;

import local.pms.authservice.dto.SignInDto;

import local.pms.authservice.dto.authuser.AuthUserDto;

import local.pms.authservice.exception.AuthenticationUserException;
import local.pms.authservice.exception.AuthenticationUserNotFoundException;

import local.pms.authservice.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

import static local.pms.authservice.constant.VersionAPI.API_V1;

@Slf4j
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/auth")
@RequiredArgsConstructor
public class AuthRestController {

    final AuthService authService;
    final AuthenticationManager authenticationManager;

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        try {
            authenticateAuthUser(signInDto);
            AuthUserDto authUser = authService.findByUsername(signInDto.username())
                    .orElseThrow(() -> new AuthenticationUserNotFoundException(signInDto.username() + " is not found"));
            log.info("User { id: {}, username: {} } successfully authenticated", authUser.id(), authUser.username());
            String token = authService.generateToken(authUser);
            return ResponseEntity.ok(createResponseModel(authUser, token));
        } catch (AuthenticationUserException | BadCredentialsException e) {
            log.info("Authorization error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findByUsername(@PathVariable("username") String username) {
        return authService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void authenticateAuthUser(SignInDto signInDto) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(signInDto.username(), signInDto.password());
        authenticationManager.authenticate(authentication);
    }

    private Map<Object, Object> createResponseModel(AuthUserDto authUserDto, String token) {
        Map<Object, Object> model = new HashMap<>();
        model.put("authUserId", authUserDto.id());
        model.put("username", authUserDto.username());
        model.put("token", token);
        return model;
    }
}