package com.wfm.experts.modules.wfm.features.timesheet.mapper;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PunchEventMapper {
    @Mapping(target = "shiftId", source = "shift.id")
    PunchEventDTO toDto(PunchEvent entity);
    @Mapping(target = "shift", ignore = true) // handled in service after auto-detection
    PunchEvent toEntity(PunchEventDTO dto);

    List<PunchEventDTO> toDtoList(List<PunchEvent> entityList);

    List<PunchEvent> toEntityList(List<PunchEventDTO> dtoList);
}
