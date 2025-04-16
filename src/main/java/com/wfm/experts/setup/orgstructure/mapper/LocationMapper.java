package com.wfm.experts.setup.orgstructure.mapper;

import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);



    // Recursively map children
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "businessUnit.id", target = "businessUnitId")
    @Mapping(target = "jobTitleIds", expression = "java(mapJobTitleIds(location.getJobTitles()))")
    @Mapping(target = "children", expression = "java(mapChildren(location.getChildren()))")
    LocationDto toDtoWithChildren(Location location);

    List<LocationDto> toDtoList(List<Location> locations);

    // Entity conversion from DTO
    @Mapping(source = "parentId", target = "parent.id")
    @Mapping(source = "businessUnitId", target = "businessUnit.id")
    Location toEntity(LocationDto dto);

    // ======== Helper Methods ========

    default List<LocationDto> mapChildren(List<Location> children) {
        if (children == null) return null;
        return children.stream().map(this::toDtoWithChildren).collect(Collectors.toList());
    }

    default List<Long> mapJobTitleIds(List<JobTitle> jobTitles) {
        if (jobTitles == null) return null;
        return jobTitles.stream().map(JobTitle::getId).collect(Collectors.toList());
    }
}
