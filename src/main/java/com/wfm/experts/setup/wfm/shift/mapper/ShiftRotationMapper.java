package com.wfm.experts.setup.wfm.shift.mapper;

import com.wfm.experts.setup.wfm.shift.dto.*;
import com.wfm.experts.setup.wfm.shift.entity.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ShiftMapper.class})
public interface ShiftRotationMapper {
    ShiftRotationDTO toDto(ShiftRotation entity);
    ShiftRotation toEntity(ShiftRotationDTO dto);

    ShiftRotationDayDTO toDayDto(ShiftRotationDay entity);
    ShiftRotationDay toDayEntity(ShiftRotationDayDTO dto);

    List<ShiftRotationDayDTO> toDayDtoList(List<ShiftRotationDay> entities);
    List<ShiftRotationDay> toDayEntityList(List<ShiftRotationDayDTO> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ShiftRotationDTO dto, @MappingTarget ShiftRotation entity);
}
