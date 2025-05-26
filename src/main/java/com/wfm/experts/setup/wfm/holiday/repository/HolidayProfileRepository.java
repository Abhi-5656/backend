package com.wfm.experts.setup.wfm.holiday.repository;

import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayProfileRepository extends JpaRepository<HolidayProfile, Long> {
    // Add custom query methods if needed
}
