package local.pms.projectservice.service.impl;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.ProjectService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    final ProjectRepository projectRepository;

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
}
