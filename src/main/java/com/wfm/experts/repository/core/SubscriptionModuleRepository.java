package com.wfm.experts.repository.core;

import com.wfm.experts.entity.core.SubscriptionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionModuleRepository extends JpaRepository<SubscriptionModule, Long> {


}
