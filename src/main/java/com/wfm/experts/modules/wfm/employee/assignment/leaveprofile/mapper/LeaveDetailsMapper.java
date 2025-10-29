package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveDetailsDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalanceLedger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeaveDetailsMapper {

    /**
     * Maps fields from the Ledger (transaction) to the DTO.
     */
    @Mapping(source = "leavePolicy.id", target = "leavePolicyId")
    @Mapping(source = "leavePolicy.policyName", target = "leavePolicyName")
    @Mapping(source = "id", target = "id") // Ledger ID
    void updateFromLedger(LeaveBalanceLedger ledger, @MappingTarget LeaveDetailsDTO dto);

    /**
     * Maps fields from the Balance (summary) to the DTO.
     */
    @Mapping(target = "id", ignore = true) // Keep the ID from the ledger
    @Mapping(target = "leavePolicyId", ignore = true) // Already mapped
    @Mapping(target = "leavePolicyName", ignore = true) // Already mapped
    @Mapping(target = "transactionType", ignore = true) // Not in balance
    @Mapping(target = "amount", ignore = true) // Not in balance
    @Mapping(target = "transactionDate", ignore = true) // Not in balance
    void updateFromBalance(LeaveBalance balance, @MappingTarget LeaveDetailsDTO dto);
}