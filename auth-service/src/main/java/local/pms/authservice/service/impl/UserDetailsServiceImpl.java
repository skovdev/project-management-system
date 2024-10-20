package local.pms.authservice.service.impl;

import local.pms.authservice.config.security.principal.UserPrincipal;

import local.pms.authservice.entity.AuthUser;

import local.pms.authservice.repository.AuthUserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service("userDetailsService")
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " is not found"));
        return new UserPrincipal(authUser.getId(), authUser.getUsername(), authUser.getPassword(), getAuthorities(authUser));
    }

    private List<GrantedAuthority> getAuthorities(AuthUser authUser) {
        return Stream.concat(
                authUser.getAuthRoles()
                        .stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getAuthority())),
                authUser.getAuthPermissions()
                        .stream()
                        .map(p -> new SimpleGrantedAuthority(p.getPermission()))
        ).collect(Collectors.toList());
    }
}