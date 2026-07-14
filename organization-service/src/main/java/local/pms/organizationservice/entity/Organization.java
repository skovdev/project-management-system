package local.pms.organizationservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_organization")
@SQLRestriction(value = "deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_organization SET deleted = true WHERE id = ?")
public class Organization extends AbstractBaseModel {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ColumnDefault(value = "false")
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
