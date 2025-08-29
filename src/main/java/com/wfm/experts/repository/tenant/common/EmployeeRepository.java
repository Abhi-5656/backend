package com.wfm.experts.repository.tenant.common;

import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.setup.roles.entity.Role;
import com.wfm.experts.tenant.common.employees.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find Employee by Employee ID
    Optional<Employee> findByEmployeeId(String employeeId);

    // Find Employee by Email (Use this for login authentication)
    Optional<Employee> findByEmail(String email);

    // ✅ Find Employees who have the given role in their roles list
    List<Employee> findByRoles(Role role);

    // (Optional) Find Employees with any of a set of roles
    List<Employee> findByRolesIn(List<Role> roles);

    long countByOrganizationalInfoEmploymentDetailsDateOfJoiningBetween(LocalDate startDate, LocalDate endDate);

    long countByOrganizationalInfoEmploymentDetailsEmploymentStatusInAndUpdatedAtBetween(Collection<EmploymentStatus> statuses, Date startDate, Date endDate);

}
