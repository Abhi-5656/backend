package com.wfm.experts.setup.wfm.leavepolicy.service;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
import java.util.List;
import java.util.Optional;

public interface LeaveProfileService {
    LeaveProfileDto createLeaveProfile(LeaveProfileDto dto);
    LeaveProfileDto updateLeaveProfile(Long id, LeaveProfileDto dto);
    void deleteLeaveProfile(Long id);
    Optional<LeaveProfileDto> getLeaveProfileById(Long id);
    List<LeaveProfileDto> getAllLeaveProfiles();
}