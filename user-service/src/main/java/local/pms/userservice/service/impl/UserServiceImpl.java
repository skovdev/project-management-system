package local.pms.userservice.service.impl;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.mapping.UserMapper;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserMapper userMapper = UserMapper.INSTANCE;

    final UserRepository userRepository;

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }
}
