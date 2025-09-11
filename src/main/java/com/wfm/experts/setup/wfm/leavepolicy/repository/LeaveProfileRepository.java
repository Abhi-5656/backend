package com.wfm.experts.setup.wfm.leavepolicy.repository;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveProfileRepository extends JpaRepository<LeaveProfile, Long> {

    /**
     * Finds a leave profile by its unique name.
     */
    Optional<LeaveProfile> findByProfileName(String profileName);

    /**
     * Checks if a leave profile with the given name exists.
     */
    boolean existsByProfileName(String profileName);
}