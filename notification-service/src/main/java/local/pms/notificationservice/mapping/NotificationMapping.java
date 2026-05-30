package local.pms.notificationservice.mapping;

import local.pms.notificationservice.dto.NotificationDto;
import local.pms.notificationservice.entity.Notification;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for converting between {@link Notification} entities and {@link NotificationDto} records.
 */
@Mapper
public interface NotificationMapping {

    NotificationMapping INSTANCE = Mappers.getMapper(NotificationMapping.class);

    /**
     * Maps a {@link Notification} entity to a {@link NotificationDto}.
     *
     * @param notification the entity to map
     * @return the corresponding DTO
     */
    NotificationDto toDto(Notification notification);

    /**
     * Maps a {@link NotificationDto} to a {@link Notification} entity.
     *
     * @param notificationDto the DTO to map
     * @return the corresponding entity
     */
    Notification toEntity(NotificationDto notificationDto);
}
