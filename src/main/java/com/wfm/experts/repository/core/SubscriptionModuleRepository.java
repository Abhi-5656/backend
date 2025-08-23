package com.wfm.experts.repository.core;

import com.wfm.experts.tenant.common.core.SubscriptionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionModuleRepository extends JpaRepository<SubscriptionModule, Long> {


}
