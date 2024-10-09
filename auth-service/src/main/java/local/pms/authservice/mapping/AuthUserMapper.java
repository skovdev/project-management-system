package local.pms.authservice.mapping;

import local.pms.authservice.dto.authuser.AuthUserDto;

import local.pms.authservice.entity.AuthUser;

import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthUserMapper {
    AuthUserMapper INSTANCE = Mappers.getMapper(AuthUserMapper.class);
    AuthUserDto toDto(AuthUser authUser);
    AuthUser toEntity(AuthUserDto authUserDto);
}
