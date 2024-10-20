package local.pms.authservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_auth_user")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthUser extends AbstractBaseModel {

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "password", nullable = false)
    String password;

    @OneToMany(mappedBy = "authUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<AuthRole> authRoles;

    @OneToMany(mappedBy = "authUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<AuthPermission> authPermissions;

}



