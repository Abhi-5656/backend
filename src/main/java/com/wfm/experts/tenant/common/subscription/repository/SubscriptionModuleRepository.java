package com.wfm.experts.tenant.common.subscription.repository;

import com.wfm.experts.tenant.common.subscription.entity.SubscriptionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionModuleRepository extends JpaRepository<SubscriptionModule, Long> {

}
