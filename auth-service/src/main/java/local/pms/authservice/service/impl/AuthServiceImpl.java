package local.pms.authservice.service.impl;

import local.pms.authservice.config.jwt.JwtTokenProvider;

import local.pms.authservice.dto.authuser.AuthUserDto;
import local.pms.authservice.dto.authuser.AuthRoleDto;
import local.pms.authservice.dto.authuser.AuthPermissionDto;

import local.pms.authservice.mapping.AuthUserMapper;

import local.pms.authservice.repository.AuthUserRepository;

import local.pms.authservice.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;

import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    final AuthUserMapper authUserMapper = AuthUserMapper.INSTANCE;

    final AuthUserRepository authUserRepository;
    final JwtTokenProvider jwtTokenProvider;

    @Override
    public Optional<AuthUserDto> findByUsername(String username) {
        return authUserRepository.findByUsername(username)
                .stream()
                .map(authUserMapper::toDto)
                .findFirst();
    }

    @Override
    public String generateToken(AuthUserDto authUserDto) {
        Map<String, Object> data = fillTokenData(authUserDto);
        return jwtTokenProvider.createToken(data);
    }

    private Map<String, Object> fillTokenData(AuthUserDto authUserDto) {
        Map<String, Object> data = new HashMap<>();
        data.put("authUserId", authUserDto.id());
        data.put("username", authUserDto.username());
        data.put("roles", getRoles(authUserDto.authRoles()));
        data.put("permissions", getPermissions(authUserDto.authPermissions()));
        return data;
    }

    private List<String> getRoles(List<AuthRoleDto> authRoles) {
        return authRoles.stream()
                .map(AuthRoleDto::authority)
                .collect(Collectors.toList());
    }

    private List<String> getPermissions(List<AuthPermissionDto> authPermissions) {
        return authPermissions.stream()
                .map(AuthPermissionDto::permission)
                .collect(Collectors.toList());
    }

}