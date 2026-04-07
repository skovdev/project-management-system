package local.pms.userservice.service;

import local.pms.userservice.config.jwt.JwtTokenProvider;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.entity.User;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("findAll returns page of DTOs")
    void should_returnPageOfDtos_when_findAll() {
        var pageable = PageRequest.of(0, 10);
        var user = buildUser(UUID.randomUUID(), UUID.randomUUID());
        var page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(page);

        var result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).firstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findById returns DTO when user exists")
    void should_returnDto_when_userExistsById() {
        var id = UUID.randomUUID();
        var user = buildUser(id, UUID.randomUUID());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var result = userService.findById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.firstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findById throws UserNotFoundException when user not found")
    void should_throwUserNotFoundException_when_findByIdNotFound() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("save persists the mapped entity")
    void should_saveEntity_when_saveCalled() {
        var dto = buildUserDto(null, UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.save(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("update returns updated DTO when caller owns the resource")
    void should_returnUpdatedDto_when_callerOwnsResource() {
        var id = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var user = buildUser(id, authUserId);
        var dto = new UserDto(id, "Bob", "Jones", "bob@mail.com", authUserId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(authUserId);
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.update(id, dto);

        assertThat(result.firstName()).isEqualTo("Bob");
        assertThat(result.lastName()).isEqualTo("Jones");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("update returns updated DTO when caller is ADMIN")
    void should_returnUpdatedDto_when_callerIsAdmin() {
        var id = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var adminAuthUserId = UUID.randomUUID();
        var user = buildUser(id, ownerId);
        var dto = new UserDto(id, "Bob", "Jones", "bob@mail.com", ownerId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(adminAuthUserId);
        when(jwtTokenProvider.extractRoles("token"))
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.update(id, dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("update throws UserNotFoundException when user not found")
    void should_throwUserNotFoundException_when_updateNotFound() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, buildUserDto(id, UUID.randomUUID())))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws UserAccessDeniedException when caller is not owner and not admin")
    void should_throwUserAccessDeniedException_when_callerNotOwner() {
        var id = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var user = buildUser(id, ownerId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(callerId);
        when(jwtTokenProvider.extractRoles("token")).thenReturn(List.of());

        assertThatThrownBy(() -> userService.update(id, buildUserDto(id, ownerId)))
                .isInstanceOf(UserAccessDeniedException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes user when caller owns the resource")
    void should_deleteUser_when_callerOwnsResource() {
        var id = UUID.randomUUID();
        var authUserId = UUID.randomUUID();
        var user = buildUser(id, authUserId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(authUserId);

        userService.delete(id);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("delete throws UserNotFoundException when user not found")
    void should_throwUserNotFoundException_when_deleteNotFound() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("delete throws UserAccessDeniedException when caller is not owner and not admin")
    void should_throwUserAccessDeniedException_when_deleteCallerNotOwner() {
        var id = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var user = buildUser(id, ownerId);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.getToken()).thenReturn("token");
        when(jwtTokenProvider.extractAuthUserId("token")).thenReturn(callerId);
        when(jwtTokenProvider.extractRoles("token")).thenReturn(List.of());

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserAccessDeniedException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteByAuthUserId removes user when found by authUserId")
    void should_deleteUser_when_authUserIdExists() {
        var authUserId = UUID.randomUUID();
        var user = buildUser(UUID.randomUUID(), authUserId);
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.of(user));

        userService.deleteByAuthUserId(authUserId);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteByAuthUserId throws UserNotFoundException when not found")
    void should_throwUserNotFoundException_when_deleteByAuthUserIdNotFound() {
        var authUserId = UUID.randomUUID();
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteByAuthUserId(authUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(authUserId.toString());

        verify(userRepository, never()).delete(any(User.class));
    }

    private User buildUser(UUID id, UUID authUserId) {
        var user = new User();
        user.setId(id);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@mail.com");
        user.setAuthUserId(authUserId);
        user.setDeleted(false);
        return user;
    }

    private UserDto buildUserDto(UUID id, UUID authUserId) {
        return new UserDto(id, "Alice", "Smith", "alice@mail.com", authUserId);
    }
}
