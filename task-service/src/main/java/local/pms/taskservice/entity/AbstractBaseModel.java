package local.pms.taskservice.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AbstractBaseModel {
    @Id
    UUID id;
}