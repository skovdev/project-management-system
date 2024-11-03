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
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "project_management_system_task")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task extends AbstractBaseModel {

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "description")
    String description;

    @Column(name = "task_status", nullable = false)
    @Enumerated(EnumType.STRING)
    TaskStatusType taskStatusType;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false)
    TaskPriorityType taskPriorityType;

    @Column(name = "active", nullable = false)
    boolean active;

    @Column(name = "project_id", nullable = false, unique = true)
    UUID projectId;

    @Column(name = "user_id", nullable = false, unique = true)
    UUID userId;

}
