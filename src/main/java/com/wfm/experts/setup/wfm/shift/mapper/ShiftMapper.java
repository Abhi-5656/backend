package com.wfm.experts.setup.wfm.shift.mapper;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    // Time and date formatting logic for complex fields only

    @Mapping(target = "startTime", expression = "java(shift.getStartTime() != null ? shift.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : null)")
    @Mapping(target = "endTime", expression = "java(shift.getEndTime() != null ? shift.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : null)")
    @Mapping(target = "createdAt", expression = "java(shift.getCreatedAt() != null ? shift.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(shift.getUpdatedAt() != null ? shift.getUpdatedAt().toString() : null)")
        // shiftName and shiftLabel map automatically (same field name in DTO/entity)
    ShiftDTO toDto(Shift shift);

    @Mapping(target = "startTime", expression = "java(dto.getStartTime() != null ? java.time.LocalTime.parse(dto.getStartTime(), java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : null)")
    @Mapping(target = "endTime", expression = "java(dto.getEndTime() != null ? java.time.LocalTime.parse(dto.getEndTime(), java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
        // shiftName and shiftLabel map automatically
    Shift toEntity(ShiftDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "startTime", expression = "java(dto.getStartTime() != null ? java.time.LocalTime.parse(dto.getStartTime(), java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : entity.getStartTime())")
    @Mapping(target = "endTime", expression = "java(dto.getEndTime() != null ? java.time.LocalTime.parse(dto.getEndTime(), java.time.format.DateTimeFormatter.ofPattern(\"HH:mm\")) : entity.getEndTime())")
        // shiftName and shiftLabel map automatically
    void updateEntityFromDto(ShiftDTO dto, @MappingTarget Shift entity);
}
