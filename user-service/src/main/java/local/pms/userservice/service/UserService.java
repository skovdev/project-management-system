package local.pms.userservice.service;

import local.pms.userservice.dto.UserDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserDto> findAll(Pageable pageable);
    void save(UserDto userDto);
    void deleteByAuthUserId(UUID authUserId);
}
