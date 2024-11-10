package local.pms.userservice.service;

import local.pms.userservice.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();
}
