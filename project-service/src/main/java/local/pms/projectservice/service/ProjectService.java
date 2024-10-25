package local.pms.projectservice.service;

import local.pms.projectservice.entity.Project;

import java.util.List;

public interface ProjectService {
    List<Project> findAll();
}
