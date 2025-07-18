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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Setter
@Getter
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_auth_user SET deleted = true WHERE id = ?")
@Table(name = "project_management_system_auth_user")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthUser extends AbstractBaseModel {

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "password", nullable = false)
    String password;

    @SQLRestriction("deleted = false")
    @OneToMany(mappedBy = "authUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<AuthRole> authRoles;

    @SQLRestriction("deleted = false")
    @OneToMany(mappedBy = "authUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<AuthPermission> authPermissions;

    @Column(name = "deleted", nullable = false)
    boolean deleted = false;

}



