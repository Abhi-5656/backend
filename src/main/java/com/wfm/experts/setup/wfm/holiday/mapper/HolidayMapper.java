package com.wfm.experts.setup.wfm.holiday.mapper;

import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HolidayMapper {

    HolidayDTO toDto(Holiday holiday);

    Holiday toEntity(HolidayDTO holidayDTO);

}
