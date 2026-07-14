package local.pms.organizationservice.mapping;

import local.pms.organizationservice.dto.OrganizationDto;

import local.pms.organizationservice.entity.Organization;

import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper
public interface OrganizationMapping {
    OrganizationMapping INSTANCE = Mappers.getMapper(OrganizationMapping.class);
    OrganizationDto toDto(Organization organization);
    Organization toEntity(OrganizationDto organizationDto);
}
