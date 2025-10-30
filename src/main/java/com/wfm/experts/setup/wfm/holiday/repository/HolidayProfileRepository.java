package com.wfm.experts.setup.wfm.holiday.repository;

import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HolidayProfileRepository extends JpaRepository<HolidayProfile, Long> {
    boolean existsByProfileName(String profileName);
    Optional<HolidayProfile> findByProfileName(String profileName);
}