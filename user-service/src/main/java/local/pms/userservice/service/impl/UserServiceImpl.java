package local.pms.userservice.service.impl;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.mapping.UserMapper;

import local.pms.userservice.repository.UserRepository;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserMapper userMapper = UserMapper.INSTANCE;

    final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(int page, int size, String sortBy, String order) {
        return userRepository.findAll(pageRequest(page, size, sortBy, order))
                .map(userMapper::toDto);
    }

    private PageRequest pageRequest(int page, int size, String sortBy, String order) {
        return PageRequest.of(page, size, sorting(sortBy, order));
    }

    private Sort sorting(String sortBy, String order) {
        return Sort.by(Sort.Order.by(sortBy)
                .with(Sort.Direction.fromString(order)));
    }

    @Override
    @Transactional
    public void save(UserDto userDto) {
        userRepository.save(userMapper.toEntity(userDto));
    }
}