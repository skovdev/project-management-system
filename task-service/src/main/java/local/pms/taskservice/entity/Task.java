package local.pms.taskservice.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import local.pms.taskservice.type.TaskPriorityType;
import local.pms.taskservice.type.TaskStatusType;

import lombok.Setter;
import lombok.Getter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_task")
@SQLRestriction(value = "deleted = false")
@SQLDelete(sql = "UPDATE project_management_system_task SET deleted = true WHERE id = ?")
public class Task extends AbstractBaseModel {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "task_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatusType taskStatusType;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false)
    private TaskPriorityType taskPriorityType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
