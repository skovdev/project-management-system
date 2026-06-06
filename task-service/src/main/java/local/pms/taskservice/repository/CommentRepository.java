package local.pms.taskservice.repository;

import local.pms.taskservice.entity.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findAllByTaskId(UUID taskId, Pageable pageable);

    Optional<Comment> findByIdAndTaskIdAndAuthorId(UUID id, UUID taskId, UUID authorId);

    Optional<Comment> findByIdAndTaskId(UUID id, UUID taskId);
}
