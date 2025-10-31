package com.wfm.experts.modules.wfm.employee.location.repository;

import com.wfm.experts.modules.wfm.employee.location.entity.EmployeeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLocationRepository extends JpaRepository<EmployeeLocation, Long> {
    // Basic CRUD operations are inherited.
    // PostGIS-specific spatial queries can be added here later using @Query
}