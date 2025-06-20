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

    // PunchEventMapper.java
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "eventTime", source = "eventTime")
    @Mapping(target = "punchType", source = "punchType")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "geoLat", source = "geoLat")
    @Mapping(target = "geoLong", source = "geoLong")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "shift", ignore = true)  // still handled separately if needed
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updatePunchEventFromDto(PunchEventDTO dto, @MappingTarget PunchEvent entity);


}
