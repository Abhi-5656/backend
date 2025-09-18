package com.wfm.experts.modules.wfm.employee.leave.mapper;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    @Mapping(source = "employee.employeeId", target = "employeeId")
    @Mapping(source = "leavePolicy.id", target = "leavePolicyId")
    LeaveRequestDTO toDto(LeaveRequest leaveRequest);

    @Mapping(target = "leavePolicy", ignore = true)
    @Mapping(target = "employee", ignore = true)
    LeaveRequest toEntity(LeaveRequestDTO leaveRequestDTO);
}