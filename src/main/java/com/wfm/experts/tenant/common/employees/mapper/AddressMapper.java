package com.wfm.experts.tenant.common.employees.mapper;

import com.wfm.experts.tenant.common.employees.dto.AddressDTO;
import com.wfm.experts.tenant.common.employees.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDTO toDto(Address address);

    Address toEntity(AddressDTO addressDTO);
}