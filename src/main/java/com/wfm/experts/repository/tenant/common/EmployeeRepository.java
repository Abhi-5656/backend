package com.wfm.experts.repository.tenant.common;

import com.wfm.experts.entity.tenant.common.Employee;
import com.wfm.experts.entity.tenant.common.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
