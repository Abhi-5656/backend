// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/mapper/LeaveBalanceMapper.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveBalanceMapper {
    @Mapping(source = "leavePolicy.policyName", target = "leavePolicyName")
    LeaveBalanceDTO toDto(LeaveBalance leaveBalance);
}