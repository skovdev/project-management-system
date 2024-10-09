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
import java.util.ArrayList;

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
        List<GrantedAuthority> roles = new ArrayList<>();
        authUser.getAuthRoles().forEach(r -> roles.add(new SimpleGrantedAuthority("ROLE_" + r.getAuthority())));
        return new UserPrincipal(authUser.getId(), authUser.getUsername(), authUser.getPassword(), roles);
    }
}