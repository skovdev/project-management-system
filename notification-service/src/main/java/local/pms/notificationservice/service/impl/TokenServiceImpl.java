package local.pms.notificationservice.service.impl;

import local.pms.notificationservice.service.TokenService;

import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

/**
 * Simple in-memory holder for the JWT bearer token for the duration of each request.
 */
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
