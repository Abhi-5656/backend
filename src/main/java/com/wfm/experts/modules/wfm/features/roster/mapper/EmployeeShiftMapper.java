package com.wfm.experts.modules.wfm.features.roster.mapper;

import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring", uses = {ShiftMapper.class})
public interface EmployeeShiftMapper {

    EmployeeShiftMapper INSTANCE = Mappers.getMapper(EmployeeShiftMapper.class);

    @Mapping(source = "shift", target = "shift") // Nested mapping, NOT shiftId
    EmployeeShiftDTO toDto(EmployeeShift entity);

    @Mapping(target = "shift", source = "shift")
    EmployeeShift toEntity(EmployeeShiftDTO dto);

}
