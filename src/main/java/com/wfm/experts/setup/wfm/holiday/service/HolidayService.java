package com.wfm.experts.setup.wfm.holiday.service;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;

import java.util.List;

public interface HolidayService {

    HolidayDTO createHoliday(HolidayDTO holidayDTO);

    HolidayDTO updateHoliday(Long id, HolidayDTO holidayDTO);

    HolidayDTO getHoliday(Long id);

    List<HolidayDTO> getAllHolidays();

    void deleteHoliday(Long id);

}
