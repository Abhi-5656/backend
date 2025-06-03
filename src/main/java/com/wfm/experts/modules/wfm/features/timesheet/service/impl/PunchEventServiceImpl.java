package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.exception.PunchEventNotFoundException;
import com.wfm.experts.modules.wfm.features.timesheet.exception.ShiftNotFoundException;
import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PunchEventServiceImpl implements PunchEventService {

    private final PunchEventRepository punchEventRepository;
    private final ShiftRepository shiftRepository;
    private final PunchEventMapper punchEventMapper;
    private final TimesheetCalculationService timesheetCalculationService;

    @Override
    public PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO) {
        PunchEvent punchEvent = punchEventMapper.toEntity(punchEventDTO);

        // Detect and set shift for IN punches
        if (punchEvent.getPunchType() == PunchType.IN) {
            Shift matchedShift = detectShiftForPunch(punchEvent.getEmployeeId(), punchEvent.getEventTime());
            if (matchedShift == null) {
                throw new ShiftNotFoundException("No shift found for date: " + punchEvent.getEventTime().toLocalDate()
                        + " and punch time: " + punchEvent.getEventTime().toLocalTime());
            }
            punchEvent.setShift(matchedShift);
        }

        PunchEvent saved = punchEventRepository.save(punchEvent);

        // Trigger timesheet recalculation after every punch event
        timesheetCalculationService.processPunchEvents(
                punchEvent.getEmployeeId(),
                punchEvent.getEventTime().toLocalDate()
        );

        return punchEventMapper.toDto(saved);
    }

    @Override
    public PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO) {
        PunchEvent existing = punchEventRepository.findById(id)
                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));

        PunchEvent updated = punchEventMapper.toEntity(punchEventDTO);
        updated.setId(id);

        // Shift detection logic on update for IN punches
        if (updated.getPunchType() == PunchType.IN) {
            Shift matchedShift = detectShiftForPunch(updated.getEmployeeId(), updated.getEventTime());
            if (matchedShift == null) {
                throw new ShiftNotFoundException("No shift found for date: " + updated.getEventTime().toLocalDate()
                        + " and punch time: " + updated.getEventTime().toLocalTime());
            }
            updated.setShift(matchedShift);
        } else {
            updated.setShift(null);
        }

        PunchEvent saved = punchEventRepository.save(updated);

        // Trigger timesheet recalculation after every punch event update
        timesheetCalculationService.processPunchEvents(
                updated.getEmployeeId(),
                updated.getEventTime().toLocalDate()
        );

        return punchEventMapper.toDto(saved);
    }

    @Override
    public Optional<PunchEventDTO> getPunchEventById(Long id) {
        return punchEventRepository.findById(id)
                .map(punchEventMapper::toDto);
    }

    @Override
    public List<PunchEventDTO> getPunchEventsByEmployeeAndPeriod(String employeeId, LocalDateTime start, LocalDateTime end) {
        return punchEventRepository.findByEmployeeIdAndEventTimeBetween(employeeId, start, end)
                .stream()
                .map(punchEventMapper::toDto)
                .toList();
    }

    @Override
    public List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId) {
        return punchEventRepository.findByTimesheetId(timesheetId)
                .stream()
                .map(punchEventMapper::toDto)
                .toList();
    }

    @Override
    public void deletePunchEvent(Long id) {
        PunchEvent punchEvent = punchEventRepository.findById(id)
                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));

        punchEventRepository.deleteById(id);

        // Trigger timesheet recalculation after deletion
        timesheetCalculationService.processPunchEvents(
                punchEvent.getEmployeeId(),
                punchEvent.getEventTime().toLocalDate()
        );
    }

    public List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return getPunchEventsByEmployeeAndPeriod(employeeId, start, end);
    }

    private Shift detectShiftForPunch(String employeeId, LocalDateTime punchEventTime) {
        LocalDate punchDate = punchEventTime.toLocalDate();
        LocalTime punchLocalTime = punchEventTime.toLocalTime();
        List<Shift> shifts = shiftRepository.findAllActiveShiftsForDate(punchDate);
        return shifts.stream()
                .filter(shift -> shift.getStartTime() != null)
                .filter(shift -> !shift.getStartTime().isAfter(punchLocalTime))
                .min(Comparator.comparing(shift -> Math.abs(
                        punchLocalTime.toSecondOfDay() - shift.getStartTime().toSecondOfDay())))
                .orElse(null);
    }
}
