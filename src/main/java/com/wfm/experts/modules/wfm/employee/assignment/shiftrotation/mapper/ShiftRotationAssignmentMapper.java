package com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.mapper;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.dto.ShiftRotationAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShiftRotationAssignmentMapper {

    ShiftRotationAssignmentMapper INSTANCE = Mappers.getMapper(ShiftRotationAssignmentMapper.class);

    @Mapping(source = "shiftRotation.id", target = "shiftRotationId")
    ShiftRotationAssignmentDTO toDto(ShiftRotationAssignment entity);

    @Mapping(target = "shiftRotation", ignore = true) // Set manually in service
    ShiftRotationAssignment toEntity(ShiftRotationAssignmentDTO dto);
}
