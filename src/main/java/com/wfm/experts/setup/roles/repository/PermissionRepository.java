/*
 * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 */

package com.wfm.experts.setup.roles.repository;

import com.wfm.experts.setup.roles.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);
}
