package local.pms.authservice.service;

import local.pms.authservice.config.jwt.JwtTokenProvider;

import local.pms.authservice.dto.SignUpDto;

import local.pms.authservice.dto.authuser.AuthRoleDto;
import local.pms.authservice.dto.authuser.AuthUserDto;
import local.pms.authservice.dto.authuser.AuthPermissionDto;

import local.pms.authservice.entity.AuthRole;
import local.pms.authservice.entity.AuthUser;
import local.pms.authservice.entity.AuthPermission;

import local.pms.authservice.event.UserDetailsCreatedEvent;
import local.pms.authservice.event.UserDetailsDeletedEvent;

import local.pms.authservice.exception.AuthUserSignUpException;
import local.pms.authservice.exception.AuthUserNotFoundException;
import local.pms.authservice.exception.AuthUsernameAlreadyExistsException;

import local.pms.authservice.repository.AuthUserRepository;

import local.pms.authservice.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("findById returns DTO when user exists")
    void should_returnDto_when_userExistsById() {
        var id = UUID.randomUUID();
        var authUser = buildAuthUser(id, "user@test.com");
        when(authUserRepository.findById(id)).thenReturn(Optional.of(authUser));

        var result = authService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("findById returns empty when user not found")
    void should_returnEmpty_when_userNotFoundById() {
        var id = UUID.randomUUID();
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());

        var result = authService.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername returns DTO when username exists")
    void should_returnDto_when_usernameExists() {
        var authUser = buildAuthUser(UUID.randomUUID(), "alice");
        when(authUserRepository.findByUsername("alice")).thenReturn(Optional.of(authUser));

        var result = authService.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("findByUsername returns empty when username not found")
    void should_returnEmpty_when_usernameNotFound() {
        when(authUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        var result = authService.findByUsername("ghost");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("signUp saves user and publishes UserDetailsCreatedEvent")
    void should_saveUserAndPublishEvent_when_signUpWithValidData() {
        var dto = new SignUpDto("bob", "pass123", "bob@mail.com", "Bob", "Smith");
        when(authUserRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(inv -> {
            AuthUser u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        authService.signUp(dto);

        verify(authUserRepository).save(any(AuthUser.class));
        var eventCaptor = ArgumentCaptor.forClass(UserDetailsCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        var event = eventCaptor.getValue();
        assertThat(event.userDetailsDto().firstName()).isEqualTo("Bob");
        assertThat(event.userDetailsDto().email()).isEqualTo("bob@mail.com");
    }

    @Test
    @DisplayName("signUp throws AuthUsernameAlreadyExistsException when username taken")
    void should_throwAuthUsernameAlreadyExistsException_when_usernameAlreadyExists() {
        var dto = new SignUpDto("taken", "pass", "e@mail.com", "A", "B");
        when(authUserRepository.findByUsername("taken")).thenReturn(Optional.of(new AuthUser()));

        assertThatThrownBy(() -> authService.signUp(dto))
                .isInstanceOf(AuthUsernameAlreadyExistsException.class)
                .hasMessageContaining("taken");

        verify(authUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("signUp wraps repository failure in AuthUserSignUpException")
    void should_throwAuthUserSignUpException_when_saveFails() {
        var dto = new SignUpDto("bob", "pass123", "bob@mail.com", "Bob", "Smith");
        when(authUserRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authUserRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> authService.signUp(dto))
                .isInstanceOf(AuthUserSignUpException.class);
    }

    @Test
    @DisplayName("authenticate returns AuthUserDto on valid credentials")
    void should_returnAuthUserDto_when_validCredentialsProvided() {
        var authUser = buildAuthUser(UUID.randomUUID(), "alice");
        var authentication = buildAuthentication("alice");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authUserRepository.findByUsername("alice")).thenReturn(Optional.of(authUser));

        var result = authService.authenticate("alice", "secret");

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("authenticate throws AuthUserNotFoundException when user disappears after authentication")
    void should_throwAuthUserNotFoundException_when_userNotFoundAfterAuth() {
        var authentication = buildAuthentication("ghost");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("ghost", "pass"))
                .isInstanceOf(AuthUserNotFoundException.class);
    }

    @Test
    @DisplayName("authenticate propagates BadCredentialsException on wrong password")
    void should_throwBadCredentialsException_when_badCredentialsProvided() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate("alice", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("generateToken delegates to JwtTokenProvider and returns token string")
    void should_returnToken_when_generateTokenCalled() {
        var authUserDto = buildAuthUserDto(UUID.randomUUID(), "alice");
        when(jwtTokenProvider.createToken(any(Map.class))).thenReturn("jwt.token.value");

        var token = authService.generateToken(authUserDto);

        assertThat(token).isEqualTo("jwt.token.value");
        verify(jwtTokenProvider).createToken(any(Map.class));
    }


    @Test
    @DisplayName("deleteById deletes user and publishes UserDetailsDeletedEvent")
    void should_deleteAndPublishEvent_when_userExistsForDelete() {
        var id = UUID.randomUUID();
        var authUser = buildAuthUser(id, "alice");
        when(authUserRepository.findById(id)).thenReturn(Optional.of(authUser));

        authService.deleteById(id);

        verify(authUserRepository).deleteById(id);
        verify(applicationEventPublisher).publishEvent(new UserDetailsDeletedEvent(id));
    }

    @Test
    @DisplayName("deleteById throws AuthUserNotFoundException when user not found")
    void should_throwAuthUserNotFoundException_when_userNotFoundForDelete() {
        var id = UUID.randomUUID();
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.deleteById(id))
                .isInstanceOf(AuthUserNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(authUserRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("restoreAuthUserById sets deleted=false and saves user (uses native query to find soft-deleted user)")
    void should_restoreUser_when_userExistsForRestore() {
        var id = UUID.randomUUID();
        var authUser = buildAuthUser(id, "alice");
        authUser.setDeleted(true);
        when(authUserRepository.findByIdIncludingDeleted(id)).thenReturn(Optional.of(authUser));

        authService.restoreAuthUserById(id);

        assertThat(authUser.isDeleted()).isFalse();
        verify(authUserRepository).save(authUser);
    }

    @Test
    @DisplayName("restoreAuthUserById throws AuthUserNotFoundException when user not found")
    void should_throwAuthUserNotFoundException_when_userNotFoundForRestore() {
        var id = UUID.randomUUID();
        when(authUserRepository.findByIdIncludingDeleted(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.restoreAuthUserById(id))
                .isInstanceOf(AuthUserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("isAuthUsernameExists throws when username already exists")
    void should_throwAuthUsernameAlreadyExistsException_when_usernameExists() {
        when(authUserRepository.findByUsername("taken")).thenReturn(Optional.of(new AuthUser()));

        assertThatThrownBy(() -> authService.isAuthUsernameExists("taken"))
                .isInstanceOf(AuthUsernameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("isAuthUsernameExists does not throw when username is free")
    void should_notThrow_when_usernameIsFree() {
        when(authUserRepository.findByUsername("free")).thenReturn(Optional.empty());

        authService.isAuthUsernameExists("free"); // no exception expected
    }

    private AuthUser buildAuthUser(UUID id, String username) {
        var role = new AuthRole();
        role.setAuthority("USER");

        var permission = new AuthPermission();
        permission.setPermission("READ_ALL");

        var authUser = new AuthUser();
        authUser.setId(id);
        authUser.setUsername(username);
        authUser.setPassword("hashed");
        authUser.setAuthRoles(List.of(role));
        authUser.setAuthPermissions(List.of(permission));
        authUser.setDeleted(false);
        return authUser;
    }

    private AuthUserDto buildAuthUserDto(UUID id, String username) {
        return new AuthUserDto(
                id,
                username,
                "hashed",
                List.of(new AuthRoleDto(UUID.randomUUID(), "USER", id)),
                List.of(new AuthPermissionDto(UUID.randomUUID(), "READ_ALL", id)));
    }

    private Authentication buildAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }
}
