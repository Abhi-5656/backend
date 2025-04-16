package com.wfm.experts.setup.orgstructure.repository;

import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {
    boolean existsByNameIgnoreCase(String name);
}
