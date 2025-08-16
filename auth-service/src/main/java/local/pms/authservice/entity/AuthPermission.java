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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Setter
@Getter
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_auth_permission SET deleted = true WHERE id = ?")
@Table(name = "project_management_system_auth_permission")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthPermission extends AbstractBaseModel {

    @Column(name = "permission", nullable = false)
    String permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("deleted = false")
    @JoinColumn(name = "auth_user_id", nullable = false)
    AuthUser authUser;

    @ColumnDefault(value = "false")
    @Column(name = "deleted", nullable = false)
    boolean deleted = false;

}
