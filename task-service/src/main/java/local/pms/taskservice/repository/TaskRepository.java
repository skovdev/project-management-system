package local.pms.taskservice.repository;

import local.pms.taskservice.entity.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findAllByUserId(UUID userId, Pageable pageable);
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);
    void deleteAllByProjectId(UUID projectId);
}