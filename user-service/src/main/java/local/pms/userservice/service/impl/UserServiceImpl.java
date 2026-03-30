package local.pms.userservice.service.impl;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.entity.User;

import local.pms.userservice.mapping.UserMapper;

import local.pms.userservice.exception.UserNotFoundException;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.UserService;

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

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public void save(UserDto userDto) {
        userRepository.save(userMapper.toEntity(userDto));
    }

    @Override
    @Transactional
    public void deleteByAuthUserId(UUID authUserId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("User with authUserId '" + authUserId + "' not found"));
        userRepository.delete(user);
    }
}