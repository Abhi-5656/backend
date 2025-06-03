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
        // Step 1: Save the Timesheet (excluding punch events at first)
        Timesheet timesheet = timesheetMapper.toEntity(timesheetDTO);
        timesheet.setPunchEvents(new ArrayList<>()); // Avoid JPA cascade issues
        Timesheet savedTimesheet = timesheetRepository.save(timesheet);

        // Step 2: Process and save punch events, linking them to this timesheet
        List<PunchEventDTO> savedPunchEvents = new ArrayList<>();
        if (timesheetDTO.getPunchEvents() != null && !timesheetDTO.getPunchEvents().isEmpty()) {
            for (PunchEventDTO punchEventDTO : timesheetDTO.getPunchEvents()) {
                punchEventDTO.setTimesheetId(savedTimesheet.getId()); // Set timesheet reference in DTO
                PunchEventDTO savedPunch = punchEventService.createPunchEvent(punchEventDTO);
                savedPunchEvents.add(savedPunch);
            }
        }

        // Step 3: Return TimesheetDTO with the punch events (if needed)
        TimesheetDTO result = timesheetMapper.toDto(savedTimesheet);
        result.setPunchEvents(savedPunchEvents); // Attach saved punch events to DTO
        return result;
    }

    @Override
    public TimesheetDTO updateTimesheet(Long id, TimesheetDTO timesheetDTO) {
        Timesheet existing = timesheetRepository.findById(id)
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found for id: " + id));

        timesheetDTO.setId(id);
        Timesheet updated = timesheetMapper.toEntity(timesheetDTO);
        updated.setCreatedAt(existing.getCreatedAt()); // preserve createdAt

        Timesheet saved = timesheetRepository.save(updated);

        // Optionally, handle punch events update logic here (e.g. add/update/delete punches)
        // This depends on your business rules—ask if you want full update logic!

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
