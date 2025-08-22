package com.wfm.experts.setup.wfm.leavepolicy.repository;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA repository for the LeavePolicy entity.
 */
@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    /**
     * Finds a leave policy by its unique policy name.
     *
     * @param policyName The name of the policy to find.
     * @return An Optional containing the found leave policy, or empty if not found.
     */
    Optional<LeavePolicy> findByPolicyName(String policyName);

    /**
     * Checks if a leave policy with the given name already exists.
     * This is more efficient than fetching the entire entity.
     *
     * @param policyName The name to check for.
     * @return true if a policy with the name exists, false otherwise.
     */
    boolean existsByPolicyName(String policyName);

    /**
     * Checks if a leave policy with the given leave code already exists.
     *
     * @param leaveCode The leave code to check for.
     * @return true if a policy with the code exists, false otherwise.
     */
    boolean existsByLeaveCode(String leaveCode);

}