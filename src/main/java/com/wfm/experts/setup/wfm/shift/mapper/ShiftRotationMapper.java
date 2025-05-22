package com.wfm.experts.setup.wfm.shift.mapper;

import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ShiftMapper.class})
public interface ShiftRotationMapper {
    @Mapping(target = "createdAt", expression = "java(shiftRotation.getCreatedAt() != null ? shiftRotation.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(shiftRotation.getUpdatedAt() != null ? shiftRotation.getUpdatedAt().toString() : null)")
    ShiftRotationDTO toDto(ShiftRotation shiftRotation);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShiftRotation toEntity(ShiftRotationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ShiftRotationDTO dto, @MappingTarget ShiftRotation entity);
}
