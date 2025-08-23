package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.OrganizationalInfoDTO;
import com.wfm.experts.tenant.common.employees.entity.OrganizationalInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EmploymentDetailsMapper.class, JobContextDetailsMapper.class})
public interface OrganizationalInfoMapper {

    OrganizationalInfoDTO toDto(OrganizationalInfo organizationalInfo);

    OrganizationalInfo toEntity(OrganizationalInfoDTO organizationalInfoDTO);
}