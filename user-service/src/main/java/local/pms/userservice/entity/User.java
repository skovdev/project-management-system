package local.pms.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "project_management_system_user")
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractBaseModel {

    @Column(name = "first_name", nullable = false)
    String firstName;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "email", nullable = false, unique = true)
    String email;

}
