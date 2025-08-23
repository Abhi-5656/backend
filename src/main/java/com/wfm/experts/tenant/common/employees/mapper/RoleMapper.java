package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.RoleDTO;
import com.wfm.experts.tenant.common.employees.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDto(Role role);

    Role toEntity(RoleDTO roleDTO);
}