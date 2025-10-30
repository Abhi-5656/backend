package com.wfm.experts.setup.wfm.holiday.repository;

import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    boolean existsByHolidayName(String holidayName);
    Optional<Holiday> findByHolidayName(String holidayName);
}