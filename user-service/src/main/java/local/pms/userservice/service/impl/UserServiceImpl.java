package local.pms.userservice.service.impl;

import local.pms.userservice.config.jwt.JwtTokenProvider;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.dto.api.response.AvatarUploadResponseDto;

import local.pms.userservice.entity.User;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.AvatarNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import local.pms.userservice.mapping.UserMapper;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.UserService;
import local.pms.userservice.service.TokenService;
import local.pms.userservice.service.StorageService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.time.Duration;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);

    final UserMapper userMapper = UserMapper.INSTANCE;

    final UserRepository userRepository;
    final StorageService storageService;
    final TokenService tokenService;
    final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toDtoWithPresignedUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        return toDtoWithPresignedUrl(user);
    }

    @Override
    @Transactional
    public void save(UserDto userDto) {
        userRepository.save(userMapper.toEntity(userDto));
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        checkOwnership(user);
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setEmail(userDto.email());
        return toDtoWithPresignedUrl(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        checkOwnership(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteByAuthUserId(UUID authUserId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("User with authUserId '" + authUserId + "' not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAuthUserId(UUID authUserId) {
        return userRepository.existsByAuthUserId(authUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAuthUserIdIncludingDeleted(UUID authUserId) {
        return userRepository.existsByAuthUserIdIncludingDeleted(authUserId);
    }

    @Override
    @Transactional
    public AvatarUploadResponseDto uploadAvatar(UUID id, MultipartFile file) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        checkOwnership(user);
        if (user.getAvatarUrl() != null) {
            storageService.deleteAvatar(user.getAvatarUrl());
        }
        var avatarKey = storageService.uploadAvatar(id, file);
        user.setAvatarUrl(avatarKey);
        userRepository.save(user);
        var presignedUrl = storageService.getPresignedUrl(avatarKey, PRESIGNED_URL_DURATION);
        return new AvatarUploadResponseDto(presignedUrl);
    }

    @Override
    @Transactional
    public void deleteAvatar(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        checkOwnership(user);
        if (user.getAvatarUrl() == null) {
            throw new AvatarNotFoundException("User with id '" + id + "' has no avatar to delete");
        }
        storageService.deleteAvatar(user.getAvatarUrl());
        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    private UserDto toDtoWithPresignedUrl(User user) {
        UserDto dto = userMapper.toDto(user);
        if (dto.avatarUrl() == null) return dto;
        String presignedUrl = storageService.getPresignedUrl(dto.avatarUrl(), PRESIGNED_URL_DURATION);
        return new UserDto(dto.id(), dto.firstName(), dto.lastName(), dto.email(), dto.authUserId(), presignedUrl);
    }

    private void checkOwnership(User user) {
        String token = tokenService.getToken();
        UUID tokenAuthUserId = jwtTokenProvider.extractAuthUserId(token);
        if (!user.getAuthUserId().equals(tokenAuthUserId)) {
            boolean isAdmin = jwtTokenProvider.extractRoles(token).stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new UserAccessDeniedException("Access denied to user with id '" + user.getId() + "'");
            }
        }
    }
}