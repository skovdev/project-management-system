package local.pms.taskservice.mapping;

import local.pms.taskservice.dto.CommentDto;

import local.pms.taskservice.entity.Comment;

import org.mapstruct.Named;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * MapStruct mapper for converting {@link Comment} entities to {@link CommentDto} records.
 * UUID fields are converted to their string representation via a null-safe helper.
 */
@Mapper(componentModel = "spring")
public interface CommentMapping {

    /**
     * Maps a {@link Comment} entity to a {@link CommentDto}.
     * The {@code id}, {@code taskId}, {@code authorId}, and {@code parentCommentId} UUID fields
     * are converted to strings using the null-safe {@link #uuidToString(UUID)} converter.
     * {@code replies} is not a direct entity field — the service attaches it afterward via
     * {@link CommentDto#withReplies(java.util.List)}.
     *
     * @param comment the source entity
     * @return the mapped DTO
     */
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "taskId", target = "taskId", qualifiedByName = "uuidToString")
    @Mapping(source = "authorId", target = "authorId", qualifiedByName = "uuidToString")
    @Mapping(source = "parentCommentId", target = "parentCommentId", qualifiedByName = "uuidToString")
    @Mapping(target = "replies", ignore = true)
    CommentDto toDto(Comment comment);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
