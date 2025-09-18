package com.wfm.experts.modules.wfm.employee.leave.mapper;

import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestApprovalMapper {

    @Mapping(source = "id", target = "approvalId")
    @Mapping(source = "leaveRequest.id", target = "leaveRequestId")
    @Mapping(source = "leaveRequest.employee.employeeId", target = "employeeId")
    @Mapping(source = "leaveRequest.employee.personalInfo.fullName", target = "employeeName")
    @Mapping(source = "leaveRequest.leavePolicy.policyName", target = "leavePolicyName")
    @Mapping(source = "leaveRequest.startDate", target = "startDate")
    @Mapping(source = "leaveRequest.endDate", target = "endDate")
    @Mapping(source = "leaveRequest.leaveDays", target = "leaveDays")
    @Mapping(source = "leaveRequest.reason", target = "reason")
    LeaveRequestApprovalDTO toDto(LeaveRequestApproval leaveRequestApproval);
}