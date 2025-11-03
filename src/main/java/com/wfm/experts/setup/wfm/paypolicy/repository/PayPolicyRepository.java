package com.wfm.experts.setup.wfm.paypolicy.repository;

import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayPolicyRepository extends JpaRepository<PayPolicy, Long> {
    
    Optional<PayPolicy> findByPolicyName(String policyName);
    boolean existsByPolicyName(String policyName);
}
