package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.EmployeeDTO;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        PersonalInfoMapper.class,
        OrganizationalInfoMapper.class,
        RoleMapper.class
})
public interface EmployeeMapper {

    @Mappings({
            @Mapping(source = "roles", target = "roles"),
            @Mapping(source = "reportingManager.employeeId", target = "reportingManagerId"),
            @Mapping(source = "hrManager.employeeId", target = "hrManagerId")
    })
    EmployeeDTO toDto(Employee employee);

    @Mappings({
            @Mapping(target = "roles", ignore = true),
            @Mapping(target = "reportingManager", ignore = true),
            @Mapping(target = "hrManager", ignore = true)
    })
    Employee toEntity(EmployeeDTO employeeDTO);

    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}