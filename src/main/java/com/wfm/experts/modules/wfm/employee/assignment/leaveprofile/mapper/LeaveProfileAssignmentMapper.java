package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collections; // Import for the expression

@Mapper(
        componentModel = "spring",
        imports = {Collections.class} // <-- ADD THIS LINE
)
public interface LeaveProfileAssignmentMapper {

    @Mapping(target = "employeeIds", expression = "java(Collections.singletonList(entity.getEmployeeId()))")
    LeaveProfileAssignmentDTO toDto(LeaveProfileAssignment entity);

    // This mapping is correct as is
    LeaveProfileAssignment toEntity(LeaveProfileAssignmentDTO dto);
}