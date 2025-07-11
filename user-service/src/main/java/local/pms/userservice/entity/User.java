package local.pms.userservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_user")
@SQLRestriction(value = "deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_user SET deleted = true WHERE id = ?")
public class User extends AbstractBaseModel {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "auth_user_id", nullable = false, unique = true)
    private String authUserId;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
