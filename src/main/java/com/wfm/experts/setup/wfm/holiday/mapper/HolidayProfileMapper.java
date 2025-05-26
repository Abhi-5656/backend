package com.wfm.experts.setup.wfm.holiday.mapper;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayProfileDTO;
import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { HolidayMapper.class })
public interface HolidayProfileMapper {

    HolidayProfileDTO toDto(HolidayProfile entity);

    HolidayProfile toEntity(HolidayProfileDTO dto);

}
