package local.pms.organizationservice.entity;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.UniqueConstraint;

import local.pms.organizationservice.type.OrganizationRoleType;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_organization_member",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"}),
        indexes = @Index(name = "idx_organization_member_user_id", columnList = "user_id"))
public class OrganizationMember extends AbstractBaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private OrganizationRoleType role;

    @ColumnDefault(value = "false")
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
