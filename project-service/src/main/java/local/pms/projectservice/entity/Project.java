package local.pms.projectservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import local.pms.projectservice.type.ProjectStatusType;

import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_project")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project extends AbstractBaseModel {

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    ProjectStatusType projectStatusType;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    @Column(name = "user_id", nullable = false, unique = true)
    UUID userId;

}
