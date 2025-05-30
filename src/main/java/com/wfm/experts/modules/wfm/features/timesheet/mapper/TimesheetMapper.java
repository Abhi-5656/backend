package com.wfm.experts.modules.wfm.features.timesheet.mapper;

import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = PunchEventMapper.class)
public interface TimesheetMapper {

    TimesheetDTO toDto(Timesheet entity);

    Timesheet toEntity(TimesheetDTO dto);

    List<TimesheetDTO> toDtoList(List<Timesheet> entityList);

    List<Timesheet> toEntityList(List<TimesheetDTO> dtoList);
}
