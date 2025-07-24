// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/LeaveProfileService.java
package com.wfm.experts.setup.wfm.leavepolicy.service;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDTO;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface LeaveProfileService {

    /**
     * Create a new LeaveProfile from the given DTO.
     * @param dto containing profileName and leaveIds
     * @return the created LeaveProfile as DTO (with generated id)
     */
    LeaveProfileDTO createLeaveProfile(LeaveProfileDTO dto);

    /**
     * Update an existing LeaveProfile.
     * @param id the id of the profile to update
     * @param dto containing new profileName and leaveIds
     * @return the updated LeaveProfile as DTO
     */
    LeaveProfileDTO updateLeaveProfile(Long id, LeaveProfileDTO dto);

    /**
     * Fetch a single LeaveProfile by its id.
     * @param id the profile id
     * @return the found LeaveProfile as DTO
     * @throws NoSuchElementException if not found
     */
    LeaveProfileDTO getLeaveProfileById(Long id);

    /**
     * Fetch all existing LeaveProfiles.
     * @return list of LeaveProfile DTOs
     */
    List<LeaveProfileDTO> getAllLeaveProfiles();

    /**
     * Delete a LeaveProfile by its id.
     * @param id the profile id to delete
     */
    void deleteLeaveProfile(Long id);

    /**
     * (Optional) Find a LeaveProfile by its unique name.
     * @param profileName the profile name
     * @return Optional containing DTO if found
     */
    Optional<LeaveProfileDTO> findByProfileName(String profileName);
}
