package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.EmergencyContactDTO;
import com.wfm.experts.tenant.common.employees.entity.EmergencyContact;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmergencyContactMapper {

    EmergencyContactDTO toDto(EmergencyContact emergencyContact);

    EmergencyContact toEntity(EmergencyContactDTO emergencyContactDTO);
}