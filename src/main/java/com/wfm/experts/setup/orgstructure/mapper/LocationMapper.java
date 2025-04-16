package com.wfm.experts.setup.orgstructure.mapper;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = JobTitleMapper.class)
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    // 🔁 Recursive + Full Job Mapping
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "businessUnit.id", target = "businessUnitId")
    @Mapping(target = "jobTitles", expression = "java(mapJobTitleDtos(location.getJobTitles()))")
    @Mapping(target = "children", expression = "java(mapChildren(location.getChildren()))")
    LocationDto toDtoWithChildren(Location location);

    List<LocationDto> toDtoList(List<Location> locations);

    @Mapping(source = "parentId", target = "parent.id")
    @Mapping(source = "businessUnitId", target = "businessUnit.id")
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

    // Manually map since we're using custom logic
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
