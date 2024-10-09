package local.pms.authservice.config.jwt;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.DefaultSecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configure(HttpSecurity builder) {
        JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenProvider);
        builder.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}