package local.pms.userservice.service.impl;

import local.pms.userservice.config.jwt.JwtTokenProvider;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.entity.User;

import local.pms.userservice.exception.UserNotFoundException;
import local.pms.userservice.exception.UserAccessDeniedException;

import local.pms.userservice.mapping.UserMapper;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.UserService;
import local.pms.userservice.service.TokenService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserMapper userMapper = UserMapper.INSTANCE;

    final UserRepository userRepository;
    final TokenService tokenService;
    final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id '" + id + "' not found"));
        return userMapper.toDto(user);
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
        return userMapper.toDto(userRepository.save(user));
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

    private void checkOwnership(User user) {
        String token = tokenService.getToken();
        UUID tokenAuthUserId = jwtTokenProvider.extractAuthUserId(token);
        if (!user.getAuthUserId().equals(tokenAuthUserId)) {
            boolean isAdmin = jwtTokenProvider.extractRoles(token).stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new UserAccessDeniedException("Access denied to user with id '" + user.getId() + "'");
            }
        }
    }
}