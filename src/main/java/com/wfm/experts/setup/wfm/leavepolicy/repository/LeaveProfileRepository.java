// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/repository/LeaveProfileRepository.java
package com.wfm.experts.setup.wfm.leavepolicy.repository;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveProfileRepository extends JpaRepository<LeaveProfile, Long> {

    /**
     * Find a LeaveProfile by its unique profileName.
     */
    Optional<LeaveProfile> findByProfileName(String profileName);

    /**
     * Check existence of a profile by name.
     */
    boolean existsByProfileName(String profileName);
}
