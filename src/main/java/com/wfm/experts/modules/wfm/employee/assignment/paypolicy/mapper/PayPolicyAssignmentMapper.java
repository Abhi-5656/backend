package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayPolicyAssignmentMapper {

    PayPolicyAssignment toEntity(PayPolicyAssignmentDTO dto);

    @Mapping(target = "employeeIds", expression = "java(java.util.Collections.singletonList(entity.getEmployeeId()))")
    PayPolicyAssignmentDTO toDTO(PayPolicyAssignment entity);

    List<PayPolicyAssignmentDTO> toDTOList(List<PayPolicyAssignment> entities);
}

