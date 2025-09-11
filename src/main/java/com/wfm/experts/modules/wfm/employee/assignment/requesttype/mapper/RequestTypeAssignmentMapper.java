package com.wfm.experts.modules.wfm.employee.assignment.requesttype.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.requesttype.dto.RequestTypeAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.requesttype.entity.RequestTypeAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collections;

@Mapper(
        componentModel = "spring",
        imports = {Collections.class}
)
public interface RequestTypeAssignmentMapper {

    @Mapping(target = "employeeIds", expression = "java(Collections.singletonList(entity.getEmployeeId()))")
    RequestTypeAssignmentDTO toDto(RequestTypeAssignment entity);

    RequestTypeAssignment toEntity(RequestTypeAssignmentDTO dto);
}