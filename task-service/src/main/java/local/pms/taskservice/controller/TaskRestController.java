package local.pms.taskservice.controller;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static local.pms.taskservice.constant.VersionAPI.API_V1;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/tasks")
@RequiredArgsConstructor
public class TaskRestController {

    final TaskService taskService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<Task> findAll() {
        return taskService.findAll();
    }
}
