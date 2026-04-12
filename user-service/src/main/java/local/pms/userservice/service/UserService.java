package local.pms.userservice.service;

import local.pms.userservice.dto.UserDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    void save(UserDto userDto);
    Page<UserDto> findAll(Pageable pageable);
    UserDto findById(UUID id);
    UserDto update(UUID id, UserDto userDto);
    void delete(UUID id);
    void deleteByAuthUserId(UUID authUserId);
    boolean existsByAuthUserId(UUID authUserId);
    boolean existsByAuthUserIdIncludingDeleted(UUID authUserId);
}
