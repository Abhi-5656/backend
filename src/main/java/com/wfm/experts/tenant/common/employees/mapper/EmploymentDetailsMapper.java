package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.EmploymentDetailsDTO;
import com.wfm.experts.tenant.common.employees.entity.EmploymentDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmploymentDetailsMapper {

    EmploymentDetailsDTO toDto(EmploymentDetails employmentDetails);

    EmploymentDetails toEntity(EmploymentDetailsDTO employmentDetailsDTO);
}