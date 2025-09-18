package com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.dto.RequestTypeProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttypeprofile.entity.RequestTypeProfileAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;

@Mapper(
        componentModel = "spring",
        imports = {Collections.class}
)
public interface RequestTypeProfileAssignmentMapper {

    @Mapping(target = "employeeIds", expression = "java(Collections.singletonList(entity.getEmployeeId()))")
    RequestTypeProfileAssignmentDTO toDto(RequestTypeProfileAssignment entity);

    RequestTypeProfileAssignment toEntity(RequestTypeProfileAssignmentDTO dto);
}