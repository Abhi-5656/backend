// LeavePolicyRepository.java
package com.wfm.experts.setup.wfm.leavepolicy.repository;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
    Optional<LeavePolicy> findByCode(String code);
}
