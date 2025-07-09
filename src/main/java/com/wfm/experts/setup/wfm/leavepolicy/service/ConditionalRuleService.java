// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/service/ConditionalRuleService.java
package com.wfm.experts.setup.wfm.leavepolicy.service;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ConditionalRuleDTO;
import com.wfm.experts.setup.wfm.leavepolicy.exception.ConditionalRuleNotFoundException;

import java.util.List;

public interface ConditionalRuleService {

    /**
     * Create a new conditional rule under a given policy.
     * @param policyId the parent LeavePolicy ID
     * @param dto      the conditional rule data
     * @throws com.wfm.experts.setup.wfm.leavepolicy.exception.LeavePolicyNotFoundException if parent policy not found
     */
    ConditionalRuleDTO createForPolicy(Long policyId, ConditionalRuleDTO dto);

    /**
     * Get a single conditional rule by its ID.
     * @param id the conditional rule ID
     * @throws ConditionalRuleNotFoundException if not found
     */
    ConditionalRuleDTO getById(Long id);

    /**
     * List all conditional rules for a given leave policy.
     * @param policyId the LeavePolicy ID
     * @return list of DTOs (empty if none)
     * @throws com.wfm.experts.setup.wfm.leavepolicy.exception.LeavePolicyNotFoundException if parent policy not found
     */
    List<ConditionalRuleDTO> getByPolicyId(Long policyId);

    /**
     * Update a conditional rule.
     * @param id  the rule ID
     * @param dto the new data
     * @throws ConditionalRuleNotFoundException if not found
     */
    ConditionalRuleDTO update(Long id, ConditionalRuleDTO dto);

    /**
     * Remove a conditional rule.
     * @param id the rule ID
     * @throws ConditionalRuleNotFoundException if not found
     */
    void delete(Long id);
}
