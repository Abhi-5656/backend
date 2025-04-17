package com.wfm.experts.setup.orgstructure.mapper;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = { JobTitleMapper.class, BusinessUnitMapper.class }
)
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    // Convert entity to DTO (with children)
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "businessUnit", target = "businessUnit")
    @Mapping(target = "jobTitles", expression = "java(mapJobTitleDtos(location.getJobTitles()))")
    @Mapping(target = "children", expression = "java(mapChildren(location.getChildren()))")
    LocationDto toDtoWithChildren(Location location);

    List<LocationDto> toDtoList(List<Location> locations);

    // Convert DTO to entity
    @Mapping(source = "parentId", target = "parent.id")
    @Mapping(source = "businessUnit", target = "businessUnit")
    Location toEntity(LocationDto dto);

    // ===== Helper methods =====

    default List<LocationDto> mapChildren(List<Location> children) {
        if (children == null) return null;
        return children.stream().map(this::toDtoWithChildren).collect(Collectors.toList());
    }

    default List<JobTitleDto> mapJobTitleDtos(List<JobTitle> jobTitles) {
        if (jobTitles == null) return null;
        return jobTitles.stream().map(this::mapJobTitle).collect(Collectors.toList());
    }

    default JobTitleDto mapJobTitle(JobTitle job) {
        if (job == null) return null;

        JobTitleDto dto = new JobTitleDto();
        dto.setId(job.getId());
        dto.setJobTitle(job.getJobTitle());
        dto.setShortName(job.getShortName());
        dto.setCode(job.getCode());
        dto.setSortOrder(job.getSortOrder());
        dto.setEffectiveDate(job.getEffectiveDate());
        dto.setExpirationDate(job.getExpirationDate());
        dto.setColor(job.getColor());

        return dto;
    }
}
