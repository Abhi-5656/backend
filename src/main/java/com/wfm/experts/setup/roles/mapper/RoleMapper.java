/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.mapper;

import com.wfm.experts.setup.roles.dto.RoleDto;
import com.wfm.experts.setup.roles.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleDto toDto(Role entity);

    Role toEntity(RoleDto dto);

    List<RoleDto> toDtoList(List<Role> entities);

    List<Role> toEntityList(List<RoleDto> dtos);
}
