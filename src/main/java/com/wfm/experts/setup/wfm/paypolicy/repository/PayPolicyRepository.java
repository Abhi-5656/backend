package com.wfm.experts.setup.wfm.paypolicy.repository;

import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayPolicyRepository extends JpaRepository<PayPolicy, Long> {
    // You can add custom query methods if needed, e.g.:
    // Optional<PayPolicy> findByPolicyName(String policyName);
}
