package local.pms.userservice.service.impl;

import local.pms.userservice.service.TokenService;

import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenServiceImpl implements TokenService {

    private String token;

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }
}
