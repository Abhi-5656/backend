package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.exception.TimesheetNotFoundException;
import com.wfm.experts.modules.wfm.features.timesheet.mapper.TimesheetMapper;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetMapper timesheetMapper;
    private final PunchEventService punchEventService;

    @Override
    public TimesheetDTO createTimesheet(TimesheetDTO timesheetDTO) {
        // Upsert logic: Find existing timesheet first!
        Optional<Timesheet> existingOpt = timesheetRepository.findByEmployeeIdAndWorkDate(
                timesheetDTO.getEmployeeId(),
                timesheetDTO.getWorkDate()
        );

        Timesheet timesheet;
        if (existingOpt.isPresent()) {
            // -- Update existing --
            timesheet = existingOpt.get();
            timesheet.setWorkDurationMinutes(timesheetDTO.getWorkDurationMinutes());
            timesheet.setTotalWorkDuration(timesheetDTO.getTotalWorkDuration());

            timesheet.setStatus(timesheetDTO.getStatus());
            timesheet.setRuleResultsJson(timesheetDTO.getRuleResultsJson());
            timesheet.setCalculatedAt(timesheetDTO.getCalculatedAt());
            // Optional: handle punchEvents, see note below
        } else {
            // -- Insert new --
            timesheet = timesheetMapper.toEntity(timesheetDTO);
            timesheet.setPunchEvents(new ArrayList<>());
        }

        Timesheet savedTimesheet = timesheetRepository.save(timesheet);

        // Process and save punch events, linking them to this timesheet
        List<PunchEventDTO> savedPunchEvents = new ArrayList<>();
        if (timesheetDTO.getPunchEvents() != null && !timesheetDTO.getPunchEvents().isEmpty()) {
            for (PunchEventDTO punchEventDTO : timesheetDTO.getPunchEvents()) {
                punchEventDTO.setTimesheetId(savedTimesheet.getId());
                PunchEventDTO savedPunch = punchEventService.createPunchEvent(punchEventDTO);
                savedPunchEvents.add(savedPunch);
            }
        }

        TimesheetDTO result = timesheetMapper.toDto(savedTimesheet);
        result.setPunchEvents(savedPunchEvents);
        return result;
    }

    @Override
    public TimesheetDTO updateTimesheet(Long id, TimesheetDTO timesheetDTO) {
        Timesheet existing = timesheetRepository.findById(id)
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found for id: " + id));

        timesheetDTO.setId(id);
        Timesheet updated = timesheetMapper.toEntity(timesheetDTO);
        updated.setCreatedAt(existing.getCreatedAt());

        Timesheet saved = timesheetRepository.save(updated);

        return timesheetMapper.toDto(saved);
    }

    @Override
    public Optional<TimesheetDTO> getTimesheetById(Long id) {
        return timesheetRepository.findById(id).map(timesheetMapper::toDto);
    }

    @Override
    public Optional<TimesheetDTO> getTimesheetByEmployeeAndDate(String employeeId, LocalDate workDate) {
        return timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, workDate)
                .map(timesheetMapper::toDto);
    }

    @Override
    public List<TimesheetDTO> getTimesheetsByEmployeeAndDateRange(String employeeId, LocalDate start, LocalDate end) {
        return timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end)
                .stream()
                .map(timesheetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTimesheet(Long id) {
        if (!timesheetRepository.existsById(id)) {
            throw new TimesheetNotFoundException("Timesheet not found for id: " + id);
        }
        timesheetRepository.deleteById(id);
    }
}
