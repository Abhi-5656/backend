package com.wfm.experts.setup.wfm.holiday.service;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;

import java.util.List;

public interface HolidayService {

    HolidayDTO createHoliday(HolidayDTO holidayDTO);

    HolidayDTO updateHoliday(Long id, HolidayDTO holidayDTO);

    HolidayDTO getHoliday(Long id);

    List<HolidayDTO> getAllHolidays();

    void deleteHoliday(Long id);

    /**
     * Create multiple holidays in a single call.
     *
     * @param holidayDTOs list of holidays to create
     * @return the list of created HolidayDTOs (with IDs populated)
     */
    List<HolidayDTO> createHolidays(List<HolidayDTO> holidayDTOs);

}
