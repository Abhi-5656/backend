// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/mapper/LeaveBalanceMapper.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveBalanceMapper {
    @Mapping(source = "leavePolicy.policyName", target = "leavePolicyName")
    @Mapping(source = "currentBalance", target = "currentBalance")
    @Mapping(source = "totalGranted", target = "totalGranted")
    @Mapping(source = "usedBalance", target = "usedBalance")
    @Mapping(source = "lastAccrualDate", target = "lastAccrualDate")
    @Mapping(source = "nextAccrualDate", target = "nextAccrualDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "effectiveDate", target = "effectiveDate")
    @Mapping(source = "expirationDate", target = "expirationDate")
    @Mapping(source = "createdAt", target = "createdAt") // <-- ADD THIS LINE
    @Mapping(source = "updatedAt", target = "updatedAt") // <-- ADD THIS LINE
    LeaveBalanceDTO toDto(LeaveBalance leaveBalance);
}