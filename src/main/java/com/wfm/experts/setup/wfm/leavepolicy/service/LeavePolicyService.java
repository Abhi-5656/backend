// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/LeavePolicyService.java
package com.wfm.experts.setup.wfm.leavepolicy.service;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDTO;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeavePolicyNotFoundException;

import java.util.List;

public interface LeavePolicyService {

    /**
     * Create a new LeavePolicy.
     * @param dto the data for the new policy
     * @return the saved policy
     */
    LeavePolicyDTO create(LeavePolicyDTO dto);

    /**
     * Retrieve an existing policy by its database ID.
     * @param id the policy ID
     * @throws LeavePolicyNotFoundException if not found
     */
    LeavePolicyDTO getById(Long id);

    /**
     * Retrieve an existing policy by its unique code.
     * @param code the policy code
     * @throws LeavePolicyNotFoundException if not found
     */
    LeavePolicyDTO getByCode(String code);

    /**
     * Return all leave policies in the system.
     */
    List<LeavePolicyDTO> getAll();

    /**
     * Update an existing LeavePolicy.
     * @param id  the ID of the policy to update
     * @param dto the new data
     * @throws LeavePolicyNotFoundException if the policy does not exist
     */
    LeavePolicyDTO update(Long id, LeavePolicyDTO dto);

    /**
     * Delete a policy permanently.
     * @param id the policy ID
     * @throws LeavePolicyNotFoundException if the policy does not exist
     */
    void delete(Long id);
}
