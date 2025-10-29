package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveDetailsMapper {

    @Mapping(source = "employee.employeeId", target = "employeeId")
    @Mapping(source = "leavePolicy.id", target = "leavePolicyId")
    @Mapping(source = "leavePolicy.policyName", target = "leavePolicyName")
    @Mapping(source = "relatedRequest.id", target = "relatedRequestId")
    LeaveDetailsDTO toDto(LeaveBalanceLedger entity);

    List<LeaveDetailsDTO> toDtoList(List<LeaveBalanceLedger> entities);
}