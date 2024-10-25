package local.pms.projectservice.controller;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.service.ProjectService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static local.pms.projectservice.constant.VersionAPI.API_V1;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping(API_V1 + "/projects")
@RequiredArgsConstructor
public class ProjectRestController {

    final ProjectService projectService;

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('PROJECT_READ')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Project> findAll() {
        return projectService.findAll();
    }
}
