package local.pms.projectservice.service.impl;

import local.pms.projectservice.service.TokenService;

import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenServiceImpl implements TokenService {

    String token;

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }
}
