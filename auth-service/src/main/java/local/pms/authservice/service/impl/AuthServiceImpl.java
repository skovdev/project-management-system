package local.pms.authservice.service.impl;

import local.pms.authservice.config.jwt.JwtTokenProvider;

import local.pms.authservice.dto.SignUpDto;

import local.pms.authservice.dto.authuser.AuthUserDto;
import local.pms.authservice.dto.authuser.AuthRoleDto;
import local.pms.authservice.dto.authuser.UserDetailsDto;
import local.pms.authservice.dto.authuser.AuthPermissionDto;

import local.pms.authservice.entity.AuthUser;
import local.pms.authservice.entity.AuthRole;
import local.pms.authservice.entity.AuthPermission;

import local.pms.authservice.event.UserDetailsCreatedEvent;
import local.pms.authservice.event.UserDetailsDeletedEvent;

import local.pms.authservice.exception.AuthUserSignUpException;
import local.pms.authservice.exception.AuthUserNotFoundException;

import local.pms.authservice.exception.AuthUsernameAlreadyExistsException;
import local.pms.authservice.mapping.AuthUserMapper;

import local.pms.authservice.repository.AuthUserRepository;

import local.pms.authservice.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

import java.util.UUID;

import java.util.stream.Collectors;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    static final String ROLE_USER = "USER";
    static final String READ_ALL = "READ_ALL";
    static final String AUTH_USER_ID_KEY = "authUserId";
    static final String USERNAME_KEY = "username";
    static final String ROLES_KEY = "roles";
    static final String PERMISSIONS_KEY = "permissions";

    final AuthUserMapper authUserMapper = AuthUserMapper.INSTANCE;

    final AuthUserRepository authUserRepository;
    final AuthenticationManager authenticationManager;
    final JwtTokenProvider jwtTokenProvider;
    final ApplicationEventPublisher applicationEventPublisher;
    final PasswordEncoder passwordEncoder;

    @Override
    public Optional<AuthUserDto> findById(UUID id) {
        return authUserRepository.findById(id)
                .map(authUserMapper::toDto);
    }

    @Override
    public Optional<AuthUserDto> findByUsername(String username) {
        return authUserRepository.findByUsername(username)
                .stream()
                .map(authUserMapper::toDto)
                .findFirst();
    }

    @Override
    @Transactional
    public void signUp(SignUpDto signUpDto) {
        isAuthUsernameExists(signUpDto.username());
        try {

            AuthUser authUser = buildAuthUser(signUpDto);
            AuthRole authRole = buildAuthRole(authUser);

            AuthPermission authPermission = buildAuthPermission(authUser);

            authUser.setAuthRoles(List.of(authRole));
            authUser.setAuthPermissions(List.of(authPermission));

            authUserRepository.save(authUser);

            log.info("The authentication user is saved successfully. AuthUserID: {}", authUser.getId());

            UserDetailsDto userDetailsDto = fillUserDetailsDto(signUpDto, authUser.getId());

            UserDetailsCreatedEvent event = new UserDetailsCreatedEvent(userDetailsDto);

            applicationEventPublisher.publishEvent(event);

        } catch (RuntimeException e) {
            log.error("Failed to sign up the authenticated user: {}", signUpDto.username(), e);
            throw new AuthUserSignUpException("Error occurred during sign-up.");
        }
    }

    private AuthUser buildAuthUser(SignUpDto signUpDto) {
        AuthUser authUser = new AuthUser();
        authUser.setUsername(signUpDto.username());
        authUser.setPassword(passwordEncoder.encode(signUpDto.password()));
        return authUser;
    }

    private AuthRole buildAuthRole(AuthUser authUser) {
        AuthRole authRole = new AuthRole();
        authRole.setAuthority(ROLE_USER);
        authRole.setAuthUser(authUser);
        return authRole;
    }

    private AuthPermission buildAuthPermission(AuthUser authUser) {
        AuthPermission authPermission = new AuthPermission();
        authPermission.setPermission(READ_ALL);
        authPermission.setAuthUser(authUser);
        return authPermission;
    }

    private UserDetailsDto fillUserDetailsDto(SignUpDto signUpDto, UUID authUserId) {
        return new UserDetailsDto(signUpDto.firstName(), signUpDto.lastName(), signUpDto.email(), authUserId);
    }

    @Override
    public AuthUserDto authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        String authenticatedUsername = authentication.getName();
        return findByUsername(authenticatedUsername)
                .orElseThrow(() -> new AuthUserNotFoundException(authenticatedUsername + " is not found"));
    }


    @Override
    public String generateToken(AuthUserDto authUserDto) {
        Map<String, Object> data = fillTokenData(authUserDto);
        return jwtTokenProvider.createToken(data);
    }

    private Map<String, Object> fillTokenData(AuthUserDto authUserDto) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUTH_USER_ID_KEY, authUserDto.id());
        data.put(USERNAME_KEY, authUserDto.username());
        data.put(ROLES_KEY, getRoles(authUserDto.authRoles()));
        data.put(PERMISSIONS_KEY, getPermissions(authUserDto.authPermissions()));
        return data;
    }

    private List<String> getRoles(List<AuthRoleDto> authRoles) {
        return authRoles.stream()
                .map(AuthRoleDto::authority)
                .collect(Collectors.toList());
    }

    private List<String> getPermissions(List<AuthPermissionDto> authPermissions) {
        return authPermissions.stream()
                .map(AuthPermissionDto::permission)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Optional<AuthUser> authUser = authUserRepository.findById(id);
        if (authUser.isEmpty()) {
            throw new AuthUserNotFoundException("Authentication user with ID '" + id + "' not found");
        }
        authUserRepository.deleteById(id);
        log.info("The authentication user is saved successfully. AuthUserID: {}", id);
        applicationEventPublisher.publishEvent(new UserDetailsDeletedEvent(id));
    }

    public void isAuthUsernameExists(String username) {
        if (authUserRepository.findByUsername(username).isPresent()) {
            throw new AuthUsernameAlreadyExistsException("Username '" + username + "' already exists");
        }
    }
}