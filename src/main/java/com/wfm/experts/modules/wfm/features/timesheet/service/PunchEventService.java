package com.wfm.experts.modules.wfm.features.timesheet.service;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PunchEventService {

    PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO);

    PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO);

    Optional<PunchEventDTO> getPunchEventById(Long id);

    List<PunchEventDTO> getPunchEventsByEmployeeAndPeriod(String employeeId, LocalDateTime start, LocalDateTime end);

    List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId);

    void deletePunchEvent(Long id);

    List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date);
}
