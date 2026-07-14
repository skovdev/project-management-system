package local.pms.organizationservice.mapping;

import local.pms.organizationservice.dto.OrganizationMemberDto;

import local.pms.organizationservice.entity.OrganizationMember;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.factory.Mappers;

@Mapper
public interface OrganizationMemberMapping {
    OrganizationMemberMapping INSTANCE = Mappers.getMapper(OrganizationMemberMapping.class);

    @Mapping(target = "organizationId", source = "organization.id")
    OrganizationMemberDto toDto(OrganizationMember organizationMember);
}
