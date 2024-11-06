package local.pms.userservice.mapping;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.entity.User;

import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
