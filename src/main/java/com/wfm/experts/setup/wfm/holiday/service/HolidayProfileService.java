package com.wfm.experts.setup.wfm.holiday.service;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayProfileDTO;

import java.util.List;

public interface HolidayProfileService {

    HolidayProfileDTO createProfile(HolidayProfileDTO dto);

    HolidayProfileDTO updateProfile(Long id, HolidayProfileDTO dto);

    HolidayProfileDTO getProfile(Long id);

    List<HolidayProfileDTO> getAllProfiles();

    void deleteProfile(Long id);
}
