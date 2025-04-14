package com.wfm.experts.hr.recruitmentonboarding.application.mapper;

import com.wfm.experts.hr.recruitmentonboarding.application.dto.JobApplicationDto;
import com.wfm.experts.hr.recruitmentonboarding.application.entity.JobApplication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    JobApplicationDto toDto(JobApplication entity);

    JobApplication toEntity(JobApplicationDto dto);
}
