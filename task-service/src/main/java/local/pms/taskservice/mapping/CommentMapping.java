package local.pms.taskservice.mapping;

import local.pms.taskservice.dto.CommentDto;

import local.pms.taskservice.entity.Comment;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
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
     * The {@code id}, {@code taskId}, and {@code authorId} UUID fields are converted
     * to strings using the null-safe {@link #uuidToString(UUID)} converter.
     *
     * @param comment the source entity
     * @return the mapped DTO
     */
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "taskId", target = "taskId", qualifiedByName = "uuidToString")
    @Mapping(source = "authorId", target = "authorId", qualifiedByName = "uuidToString")
    CommentDto toDto(Comment comment);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
