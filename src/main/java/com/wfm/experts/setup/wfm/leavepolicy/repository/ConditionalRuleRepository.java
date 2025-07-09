// ConditionalRuleRepository.java
package com.wfm.experts.setup.wfm.leavepolicy.repository;

import com.wfm.experts.setup.wfm.leavepolicy.entity.ConditionalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionalRuleRepository extends JpaRepository<ConditionalRule, Long> {
    List<ConditionalRule> findByLeavePolicyId(Long leavePolicyId);
}
