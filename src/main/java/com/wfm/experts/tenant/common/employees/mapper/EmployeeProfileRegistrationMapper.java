package com.wfm.experts.tenant.common.employees.mapper;


import com.wfm.experts.tenant.common.employees.entity.EmployeeProfileRegistration;
import com.wfm.experts.tenant.common.employees.dto.EmployeeProfileRegistrationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for the entity {@link EmployeeProfileRegistration} and its DTO {@link EmployeeProfileRegistrationDTO}.
 */
@Mapper(componentModel = "spring")
public interface EmployeeProfileRegistrationMapper {

    EmployeeProfileRegistrationMapper INSTANCE = Mappers.getMapper(EmployeeProfileRegistrationMapper.class);

    /**
     * Converts an EmployeeProfileRegistration entity to its DTO representation.
     *
     * @param entity The entity to convert.
     * @return The corresponding DTO.
     */
    EmployeeProfileRegistrationDTO toDto(EmployeeProfileRegistration entity);

    /**
     * Converts an EmployeeProfileRegistrationDTO to its entity representation.
     * The 'id' field is ignored to prevent overwriting the primary key on updates.
     *
     * @param dto The DTO to convert.
     * @return The corresponding entity.
     */
    @Mapping(target = "id", ignore = true)
    EmployeeProfileRegistration toEntity(EmployeeProfileRegistrationDTO dto);
}