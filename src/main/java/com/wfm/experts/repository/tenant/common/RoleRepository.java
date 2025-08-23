package com.wfm.experts.repository.tenant.common;

import com.wfm.experts.tenant.common.employees.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * ✅ Find Role by Role Name
     *
     * @param roleName Name of the Role (ADMIN, MANAGER, EMPLOYEE, etc.)
     * @return Optional Role entity
     */
    Optional<Role> findByRoleName(String roleName);
}
