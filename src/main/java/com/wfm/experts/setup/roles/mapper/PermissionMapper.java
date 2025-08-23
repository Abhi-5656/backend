/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.mapper;

import com.wfm.experts.setup.roles.dto.PermissionDto;
import com.wfm.experts.setup.roles.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    PermissionDto toDto(Permission entity);

    Permission toEntity(PermissionDto dto);

    List<PermissionDto> toDtoList(List<Permission> entities);

    List<Permission> toEntityList(List<PermissionDto> dtos);
}
