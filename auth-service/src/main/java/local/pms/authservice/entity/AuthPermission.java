package local.pms.authservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_auth_permission")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthPermission extends AbstractBaseModel {

    @Column(name = "permission", nullable = false)
    String permission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auth_user_id", nullable = false)
    AuthUser authUser;

}