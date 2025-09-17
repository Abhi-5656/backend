package com.wfm.experts.setup.wfm.requesttype.repository;

import com.wfm.experts.setup.wfm.requesttype.entity.RequestTypeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestTypeProfileRepository extends JpaRepository<RequestTypeProfile, Long> {

    /**
     * Finds a request type profile by its unique name.
     */
    Optional<RequestTypeProfile> findByProfileName(String profileName);

    /**
     * Checks if a request type profile with the given name exists.
     */
    boolean existsByProfileName(String profileName);
}