package com.wfm.experts.setup.wfm.holiday.repository;

import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    // Add custom queries if needed
}
