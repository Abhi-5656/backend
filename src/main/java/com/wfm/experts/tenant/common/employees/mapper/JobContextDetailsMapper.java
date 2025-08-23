package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.JobContextDetailsDTO;
import com.wfm.experts.tenant.common.employees.entity.JobContextDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobContextDetailsMapper {

    JobContextDetailsDTO toDto(JobContextDetails jobContextDetails);

    JobContextDetails toEntity(JobContextDetailsDTO jobContextDetailsDTO);
}