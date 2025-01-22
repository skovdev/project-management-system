package local.pms.userservice.service;

import local.pms.userservice.dto.UserDto;

import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    Page<UserDto> findAll(int page, int size, String sortBy, String order);
    void save(UserDto userDto);
    void deleteById(UUID id);
}
