package local.pms.projectservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import local.pms.projectservice.type.ProjectStatusType;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_project")
@SQLRestriction(value = "deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_project SET deleted = true WHERE id = ?")
public class Project extends AbstractBaseModel {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    private ProjectStatusType projectStatusType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
