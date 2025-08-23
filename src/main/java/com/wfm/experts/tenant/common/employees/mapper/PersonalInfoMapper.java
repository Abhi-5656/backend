package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.PersonalInfoDTO;
import com.wfm.experts.tenant.common.employees.entity.PersonalInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, EmergencyContactMapper.class})
public interface PersonalInfoMapper {

    PersonalInfoDTO toDto(PersonalInfo personalInfo);

    PersonalInfo toEntity(PersonalInfoDTO personalInfoDTO);
}