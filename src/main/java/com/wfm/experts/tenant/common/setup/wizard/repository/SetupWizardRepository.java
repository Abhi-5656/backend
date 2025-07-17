package com.wfm.experts.tenant.common.setup.wizard.repository;

import com.wfm.experts.tenant.common.setup.wizard.entity.SetupWizard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link SetupWizard} entity.
 * This repository manages the lifecycle of the setup wizard data, which is
 * stored in the public schema.
 */
@Repository
public interface SetupWizardRepository extends JpaRepository<SetupWizard, Long> {
    // You can add custom query methods here if needed in the future.
    // For example, to find an incomplete wizard by email:
    // Optional<SetupWizard> findByAdminEmailAndStatus(String adminEmail, String status);
}