package local.pms.projectservice.service.impl;

import local.pms.projectservice.dto.ProjectDto;

import local.pms.projectservice.mapping.ProjectMapping;

import local.pms.projectservice.repository.ProjectRepository;

import local.pms.projectservice.service.ProjectService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    final ProjectMapping projectMapping = ProjectMapping.INSTANCE;

    final ProjectRepository projectRepository;

    @Override
    public Page<ProjectDto> findAll(int page, int size, String sortBy, String order) {
        return projectRepository.findAll(pageRequest(page, size, sortBy, order))
                .map(projectMapping::toDto);
    }

    private PageRequest pageRequest(int page, int size, String sortBy, String order) {
        return PageRequest.of(page, size, sorting(sortBy, order));
    }

    private Sort sorting(String sortBy, String order) {
        return Sort.by(Sort.Order.by(sortBy)
                .with(Sort.Direction.fromString(order)));
    }
}
