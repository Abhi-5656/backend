package com.wfm.experts.setup.wfm.leavepolicy.service;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDto;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Leave Policies.
 * Defines the contract for business operations related to leave policies.
 */
public interface LeavePolicyService {

    /**
     * Creates a new leave policy based on the provided DTO.
     *
     * @param leavePolicyDto The DTO containing the details of the policy to create.
     * @return The created LeavePolicyDto, including the generated ID.
     */
    LeavePolicyDto createLeavePolicy(LeavePolicyDto leavePolicyDto);

    /**
     * Retrieves a list of all leave policies.
     *
     * @return A list of LeavePolicyDto objects.
     */
    List<LeavePolicyDto> getAllLeavePolicies();

    /**
     * Finds a leave policy by its unique ID.
     *
     * @param id The ID of the leave policy.
     * @return An Optional containing the LeavePolicyDto if found, otherwise empty.
     */
    Optional<LeavePolicyDto> getLeavePolicyById(Long id);

    /**
     * Updates an existing leave policy.
     *
     * @param id The ID of the policy to update.
     * @param leavePolicyDto The DTO with the updated details.
     * @return The updated LeavePolicyDto.
     */
    LeavePolicyDto updateLeavePolicy(Long id, LeavePolicyDto leavePolicyDto);

    /**
     * Deletes a leave policy by its ID.
     *
     * @param id The ID of the policy to delete.
     */
    void deleteLeavePolicy(Long id);
}