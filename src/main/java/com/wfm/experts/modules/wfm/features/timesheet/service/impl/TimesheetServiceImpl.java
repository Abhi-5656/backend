package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.dto.TimesheetDTO;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.exception.TimesheetNotFoundException;
import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
import com.wfm.experts.modules.wfm.features.timesheet.mapper.TimesheetMapper;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetMapper timesheetMapper;
    private final PunchEventService punchEventService;
    private final PunchEventMapper punchEventMapper;
    private final TimesheetCalculationService timesheetCalculationService;

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
            timesheet.setRegularHoursMinutes(timesheetDTO.getRegularHoursMinutes());
            timesheet.setExcessHoursMinutes(timesheetDTO.getExcessHoursMinutes());
            timesheet.setTotalWorkDurationMinutes(timesheetDTO.getTotalWorkDurationMinutes());
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

        // --- Only update allowed fields (not employeeId, createdAt, etc.)
        timesheetMapper.updateTimesheetFromDto(timesheetDTO, existing);

        // --- Handle punch events update (merge instead of replace) ---
        if (timesheetDTO.getPunchEvents() != null && !timesheetDTO.getPunchEvents().isEmpty()) {
            Map<Long, PunchEvent> existingPunchMap = existing.getPunchEvents().stream()
                    .collect(Collectors.toMap(PunchEvent::getId, Function.identity()));

            for (PunchEventDTO incomingDto : timesheetDTO.getPunchEvents()) {
                if (incomingDto.getId() != null && existingPunchMap.containsKey(incomingDto.getId())) {
                    PunchEvent existingPunch = existingPunchMap.get(incomingDto.getId());
                    punchEventMapper.updatePunchEventFromDto(incomingDto, existingPunch);
                    existingPunch.setUpdatedAt(LocalDateTime.now());
                } else {
                    PunchEvent newPunch = punchEventMapper.toEntity(incomingDto);
                    newPunch.setTimesheet(existing);
                    newPunch.setCreatedAt(LocalDateTime.now());
                    existing.getPunchEvents().add(newPunch);
                }
            }
        }

        existing.setUpdatedAt(LocalDateTime.now());
        Timesheet saved = timesheetRepository.save(existing);

        // 🔁 Recalculate totals & rule results based on updated punches
        timesheetCalculationService.processPunchEvents(existing.getEmployeeId(), existing.getWorkDate());

        return timesheetMapper.toDto(saved);
    }

    @Override
    public List<TimesheetDTO> createTimesheets(List<TimesheetDTO> timesheetDTOs) {
        if (timesheetDTOs == null || timesheetDTOs.isEmpty()) {
            return new ArrayList<>();
        }
        return timesheetDTOs.stream()
                .map(this::createTimesheet)
                .collect(Collectors.toList());
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
