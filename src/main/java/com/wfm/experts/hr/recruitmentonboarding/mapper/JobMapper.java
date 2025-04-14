package com.wfm.experts.hr.recruitmentonboarding.mapper;

import com.wfm.experts.hr.recruitmentonboarding.dto.JobDto;
import com.wfm.experts.hr.recruitmentonboarding.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface JobMapper {



    // Entity -> DTO
    JobDto toDto(Job job);

    // DTO -> Entity
    Job toEntity(JobDto dto);
}
