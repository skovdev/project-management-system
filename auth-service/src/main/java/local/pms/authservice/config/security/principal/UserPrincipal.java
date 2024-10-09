package local.pms.authservice.config.security.principal;

import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.User;

import java.util.UUID;
import java.util.Collection;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPrincipal extends User {

    private final UUID id;

    public UserPrincipal(UUID id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}