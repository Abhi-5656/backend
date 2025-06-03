package com.wfm.experts.modules.wfm.employee.assignment.paypolicy.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.dto.PayPolicyAssignmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayPolicyAssignmentMapper {

    @Mapping(source = "effectiveDate", target = "effectiveDate")
    @Mapping(source = "expirationDate", target = "expirationDate")
    @Mapping(source = "assignedAt", target = "assignedAt")
    PayPolicyAssignment toEntity(PayPolicyAssignmentDTO dto);

    @Mapping(source = "effectiveDate", target = "effectiveDate")
    @Mapping(source = "expirationDate", target = "expirationDate")
    @Mapping(source = "assignedAt", target = "assignedAt")
    PayPolicyAssignmentDTO toDTO(PayPolicyAssignment entity);

    List<PayPolicyAssignmentDTO> toDTOList(List<PayPolicyAssignment> entities);

}
