package com.wfm.experts.repository.tenant.common;

import com.wfm.experts.entity.tenant.common.EmployeeProfileRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link EmployeeProfileRegistration} entity.
 * Provides methods to query employee profile registration records using the employeeId.
 */
@Repository
public interface EmployeeProfileRegistrationRepository extends JpaRepository<EmployeeProfileRegistration, Long> {

    /**
     * Finds a registration record by the employee's unique ID.
     *
     * @param employeeId The employee's ID.
     * @return An {@link Optional} containing the registration record if found.
     */
    Optional<EmployeeProfileRegistration> findByEmployeeId(String employeeId);

    /**
     * Checks if a registration record exists for a given employee ID.
     * This is more efficient than fetching the entire record if you only need to check for existence.
     *
     * @param employeeId The employee's ID.
     * @return {@code true} if a record exists, {@code false} otherwise.
     */
    boolean existsByEmployeeId(String employeeId);
}