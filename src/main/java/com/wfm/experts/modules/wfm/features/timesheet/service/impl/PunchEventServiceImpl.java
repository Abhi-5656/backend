//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.exception.PunchEventNotFoundException;
//import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
//import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
//import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PunchEventServiceImpl implements PunchEventService {
//
//    private final PunchEventRepository punchEventRepository;
//    private final ShiftRepository shiftRepository;
//    private final PunchEventMapper punchEventMapper;
//    private final TimesheetCalculationService timesheetCalculationService;
//
//    @Override
//    public PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO) {
//        PunchEvent punchEvent = punchEventMapper.toEntity(punchEventDTO);
//
//        // 🛠 Set Timesheet relation manually if DTO has ID and it's not already mapped
//        if (punchEventDTO.getTimesheetId() != null && punchEvent.getTimesheet() == null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            punchEvent.setTimesheet(timesheet);
//        }
//
//        // 🔒 Optional: Enable shift detection if required
////        if (punchEvent.getPunchType() == PunchType.IN) {
////            Shift matchedShift = detectShiftForPunch(punchEvent.getEmployeeId(), punchEvent.getEventTime());
////            if (matchedShift == null) {
////                throw new ShiftNotFoundException("No shift found for date: " + punchEvent.getEventTime().toLocalDate()
////                        + " and punch time: " + punchEvent.getEventTime().toLocalTime());
////            }
////            punchEvent.setShift(matchedShift);
////        }
//
//        PunchEvent saved = punchEventRepository.save(punchEvent);
//
//        if (punchEvent.getEventTime() != null) {
//            timesheetCalculationService.processPunchEvents(
//                    punchEvent.getEmployeeId(),
//                    punchEvent.getEventTime().toLocalDate()
//            );
//        }
//
//        return punchEventMapper.toDto(saved);
//    }
//
//    @Override
//    public PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO) {
//        PunchEvent existing = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        // Only update fields that are allowed (employeeId is NOT updated)
//        punchEventMapper.updatePunchEventFromDto(punchEventDTO, existing);
//
//        if (punchEventDTO.getEventTime() != null) {
//            existing.setEventTime(punchEventDTO.getEventTime()); // ensure time is updated
//        }
//
//        if (punchEventDTO.getTimesheetId() != null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            existing.setTimesheet(timesheet);
//        }
//
//        PunchEvent saved = punchEventRepository.save(existing);
//
//        if (existing.getEventTime() != null) {
//            timesheetCalculationService.processPunchEvents(
//                    existing.getEmployeeId(),
//                    existing.getEventTime().toLocalDate()
//            );
//        }
//
//        return punchEventMapper.toDto(saved);
//    }
//
//
//
//
//    @Override
//    public Optional<PunchEventDTO> getPunchEventById(Long id) {
//        return punchEventRepository.findById(id)
//                .map(punchEventMapper::toDto);
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndPeriod(String employeeId, LocalDateTime start, LocalDateTime end) {
//        return punchEventRepository.findByEmployeeIdAndEventTimeBetween(employeeId, start, end)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId) {
//        return punchEventRepository.findByTimesheetId(timesheetId)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void deletePunchEvent(Long id) {
//        PunchEvent punchEvent = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        punchEventRepository.deleteById(id);
//
//        if (punchEvent.getEventTime() != null) {
//            timesheetCalculationService.processPunchEvents(
//                    punchEvent.getEmployeeId(),
//                    punchEvent.getEventTime().toLocalDate()
//            );
//        }
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date) {
//        LocalDateTime start = date.atStartOfDay();
//        LocalDateTime end = date.atTime(LocalTime.MAX);
//        return getPunchEventsByEmployeeAndPeriod(employeeId, start, end);
//    }
//
//    // 🔁 Optional: Shift detection logic (use if required)
////    private Shift detectShiftForPunch(String employeeId, LocalDateTime punchEventTime) {
////        LocalDate punchDate = punchEventTime.toLocalDate();
////        LocalTime punchLocalTime = punchEventTime.toLocalTime();
////        List<Shift> shifts = shiftRepository.findAllActiveShiftsForDate(punchDate);
////        return shifts.stream()
////                .filter(shift -> shift.getStartTime() != null)
////                .filter(shift -> !shift.getStartTime().isAfter(punchLocalTime))
////                .min(Comparator.comparing(shift -> Math.abs(
////                        punchLocalTime.toSecondOfDay() - shift.getStartTime().toSecondOfDay())))
////                .orElse(null);
////    }
//}
//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
//import com.wfm.experts.modules.wfm.features.timesheet.exception.PunchEventNotFoundException;
//import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
//import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
//import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PunchEventServiceImpl implements PunchEventService {
//
//    private final PunchEventRepository punchEventRepository;
//    private final ShiftRepository shiftRepository;
//    private final PunchEventMapper punchEventMapper;
//    private final TimesheetCalculationService timesheetCalculationService;
//
//    @Override
//    public PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO) {
//        PunchEvent punchEvent = punchEventMapper.toEntity(punchEventDTO);
//
//        if (punchEventDTO.getTimesheetId() != null && punchEvent.getTimesheet() == null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            punchEvent.setTimesheet(timesheet);
//        }
//
//        PunchEvent saved = punchEventRepository.save(punchEvent);
//
//        recalculateTimesheetForPunch(saved);
//
//        return punchEventMapper.toDto(saved);
//    }
//
//    @Override
//    public PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO) {
//        PunchEvent existing = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        punchEventMapper.updatePunchEventFromDto(punchEventDTO, existing);
//
//        if (punchEventDTO.getEventTime() != null) {
//            existing.setEventTime(punchEventDTO.getEventTime());
//        }
//
//        if (punchEventDTO.getTimesheetId() != null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            existing.setTimesheet(timesheet);
//        }
//
//        PunchEvent saved = punchEventRepository.save(existing);
//
//        recalculateTimesheetForPunch(saved);
//
//        return punchEventMapper.toDto(saved);
//    }
//
//    @Override
//    public void deletePunchEvent(Long id) {
//        PunchEvent punchEvent = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        punchEventRepository.deleteById(id);
//
//        recalculateTimesheetForPunch(punchEvent);
//    }
//
//    private void recalculateTimesheetForPunch(PunchEvent punchEvent) {
//        LocalDate workDateToRecalculate = findWorkDateForRecalculation(punchEvent);
//        if (workDateToRecalculate != null) {
//            timesheetCalculationService.processPunchEvents(
//                    punchEvent.getEmployeeId(),
//                    workDateToRecalculate
//            );
//        }
//    }
//
//    private LocalDate findWorkDateForRecalculation(PunchEvent punchEvent) {
//        if (punchEvent.getPunchType() == PunchType.IN) {
//            return punchEvent.getEventTime().toLocalDate();
//        } else if (punchEvent.getPunchType() == PunchType.OUT) {
//            Optional<PunchEvent> lastInPunch = punchEventRepository
//                    .findFirstByEmployeeIdAndPunchTypeAndEventTimeBeforeOrderByEventTimeDesc(
//                            punchEvent.getEmployeeId(),
//                            PunchType.IN,
//                            punchEvent.getEventTime());
//            return lastInPunch.map(inPunch -> inPunch.getEventTime().toLocalDate())
//                    .orElse(punchEvent.getEventTime().toLocalDate());
//        }
//        return punchEvent.getEventTime().toLocalDate();
//    }
//
//    // ... (rest of the service methods remain unchanged)
//    @Override
//    public Optional<PunchEventDTO> getPunchEventById(Long id) {
//        return punchEventRepository.findById(id)
//                .map(punchEventMapper::toDto);
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndPeriod(String employeeId, LocalDateTime start, LocalDateTime end) {
//        return punchEventRepository.findByEmployeeIdAndEventTimeBetween(employeeId, start, end)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId) {
//        return punchEventRepository.findByTimesheetId(timesheetId)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date) {
//        LocalDateTime start = date.atStartOfDay();
//        LocalDateTime end = date.atTime(LocalTime.MAX);
//        return getPunchEventsByEmployeeAndPeriod(employeeId, start, end);
//    }
//}

//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
//import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
//import com.wfm.experts.modules.wfm.features.timesheet.exception.PunchEventNotFoundException;
//import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
//import com.wfm.experts.modules.wfm.features.timesheet.producer.PunchProcessingProducer;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
//import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PunchEventServiceImpl implements PunchEventService {
//
//    private final PunchEventRepository punchEventRepository;
//    private final ShiftRepository shiftRepository;
//    private final PunchEventMapper punchEventMapper;
//    private final PunchProcessingProducer punchProcessingProducer;
//
//    @Override
//    public PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO) {
//        PunchEvent punchEvent = punchEventMapper.toEntity(punchEventDTO);
//
//        if (punchEventDTO.getTimesheetId() != null && punchEvent.getTimesheet() == null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            punchEvent.setTimesheet(timesheet);
//        }
//
//        PunchEvent saved = punchEventRepository.save(punchEvent);
//
//        recalculateTimesheetForPunch(saved);
//
//        return punchEventMapper.toDto(saved);
//    }
//
//    @Override
//    public PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO) {
//        PunchEvent existing = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        punchEventMapper.updatePunchEventFromDto(punchEventDTO, existing);
//
//        if (punchEventDTO.getEventTime() != null) {
//            existing.setEventTime(punchEventDTO.getEventTime());
//        }
//
//        if (punchEventDTO.getTimesheetId() != null) {
//            Timesheet timesheet = new Timesheet();
//            timesheet.setId(punchEventDTO.getTimesheetId());
//            existing.setTimesheet(timesheet);
//        }
//
//        PunchEvent saved = punchEventRepository.save(existing);
//
//        recalculateTimesheetForPunch(saved);
//
//        return punchEventMapper.toDto(saved);
//    }
//
//    @Override
//    public void deletePunchEvent(Long id) {
//        PunchEvent punchEvent = punchEventRepository.findById(id)
//                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));
//
//        punchEventRepository.deleteById(id);
//
//        recalculateTimesheetForPunch(punchEvent);
//    }
//
//    private void recalculateTimesheetForPunch(PunchEvent punchEvent) {
//        LocalDate workDateToRecalculate = findWorkDateForRecalculation(punchEvent);
//        if (workDateToRecalculate != null) {
//            punchProcessingProducer.sendPunchProcessingRequest(
//                    new PunchProcessingRequest(punchEvent.getEmployeeId(), workDateToRecalculate)
//            );
//        }
//    }
//
//    private LocalDate findWorkDateForRecalculation(PunchEvent punchEvent) {
//        if (punchEvent.getPunchType() == PunchType.IN) {
//            return punchEvent.getEventTime().toLocalDate();
//        } else if (punchEvent.getPunchType() == PunchType.OUT) {
//            Optional<PunchEvent> lastInPunch = punchEventRepository
//                    .findFirstByEmployeeIdAndPunchTypeAndEventTimeBeforeOrderByEventTimeDesc(
//                            punchEvent.getEmployeeId(),
//                            PunchType.IN,
//                            punchEvent.getEventTime());
//            return lastInPunch.map(inPunch -> inPunch.getEventTime().toLocalDate())
//                    .orElse(punchEvent.getEventTime().toLocalDate());
//        }
//        return punchEvent.getEventTime().toLocalDate();
//    }
//
//    @Override
//    public Optional<PunchEventDTO> getPunchEventById(Long id) {
//        return punchEventRepository.findById(id)
//                .map(punchEventMapper::toDto);
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndPeriod(String employeeId, LocalDateTime start, LocalDateTime end) {
//        return punchEventRepository.findByEmployeeIdAndEventTimeBetween(employeeId, start, end)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId) {
//        return punchEventRepository.findByTimesheetId(timesheetId)
//                .stream()
//                .map(punchEventMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date) {
//        LocalDateTime start = date.atStartOfDay();
//        LocalDateTime end = date.atTime(LocalTime.MAX);
//        return getPunchEventsByEmployeeAndPeriod(employeeId, start, end);
//    }
//}

package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchEventDTO;
import com.wfm.experts.modules.wfm.features.timesheet.dto.PunchProcessingRequest;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.exception.PunchEventNotFoundException;
import com.wfm.experts.modules.wfm.features.timesheet.mapper.PunchEventMapper;
import com.wfm.experts.modules.wfm.features.timesheet.producer.PunchProcessingProducer;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
import com.wfm.experts.modules.wfm.features.timesheet.service.PunchEventService;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PunchEventServiceImpl implements PunchEventService {

    private final PunchEventRepository punchEventRepository;
    private final ShiftRepository shiftRepository;
    private final PunchEventMapper punchEventMapper;
    private final PunchProcessingProducer punchProcessingProducer;

    @Override
    public PunchEventDTO createPunchEvent(PunchEventDTO punchEventDTO) {
        PunchEvent punchEvent = punchEventMapper.toEntity(punchEventDTO);

        if (punchEventDTO.getTimesheetId() != null && punchEvent.getTimesheet() == null) {
            Timesheet timesheet = new Timesheet();
            timesheet.setId(punchEventDTO.getTimesheetId());
            punchEvent.setTimesheet(timesheet);
        }

        PunchEvent saved = punchEventRepository.save(punchEvent);

        recalculateTimesheetForPunch(saved);

        return punchEventMapper.toDto(saved);
    }

    @Override
    public PunchEventDTO updatePunchEvent(Long id, PunchEventDTO punchEventDTO) {
        PunchEvent existing = punchEventRepository.findById(id)
                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));

        punchEventMapper.updatePunchEventFromDto(punchEventDTO, existing);

        if (punchEventDTO.getEventTime() != null) {
            existing.setEventTime(punchEventDTO.getEventTime());
        }

        if (punchEventDTO.getTimesheetId() != null) {
            Timesheet timesheet = new Timesheet();
            timesheet.setId(punchEventDTO.getTimesheetId());
            existing.setTimesheet(timesheet);
        }

        PunchEvent saved = punchEventRepository.save(existing);

        recalculateTimesheetForPunch(saved);

        return punchEventMapper.toDto(saved);
    }

    @Override
    public void deletePunchEvent(Long id) {
        PunchEvent punchEvent = punchEventRepository.findById(id)
                .orElseThrow(() -> new PunchEventNotFoundException("PunchEvent not found: " + id));

        punchEventRepository.deleteById(id);

        recalculateTimesheetForPunch(punchEvent);
    }

    private void recalculateTimesheetForPunch(PunchEvent punchEvent) {
        LocalDate workDateToRecalculate = findWorkDateForRecalculation(punchEvent);
        if (workDateToRecalculate != null) {
            punchProcessingProducer.sendPunchProcessingRequest(
                    new PunchProcessingRequest(punchEvent.getEmployeeId(), workDateToRecalculate)
            );
        }
    }

    private LocalDate findWorkDateForRecalculation(PunchEvent punchEvent) {
        if (punchEvent.getPunchType() == PunchType.IN) {
            return punchEvent.getEventTime().toLocalDate();
        } else if (punchEvent.getPunchType() == PunchType.OUT) {
            Optional<PunchEvent> lastInPunch = punchEventRepository
                    .findFirstByEmployeeIdAndPunchTypeAndEventTimeBeforeOrderByEventTimeDesc(
                            punchEvent.getEmployeeId(),
                            PunchType.IN,
                            punchEvent.getEventTime());
            return lastInPunch.map(inPunch -> inPunch.getEventTime().toLocalDate())
                    .orElse(punchEvent.getEventTime().toLocalDate());
        }
        return punchEvent.getEventTime().toLocalDate();
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
                .collect(Collectors.toList());
    }

    @Override
    public List<PunchEventDTO> getPunchEventsByTimesheetId(Long timesheetId) {
        return punchEventRepository.findByTimesheetId(timesheetId)
                .stream()
                .map(punchEventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PunchEventDTO> getPunchEventsByEmployeeAndDate(String employeeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return getPunchEventsByEmployeeAndPeriod(employeeId, start, end);
    }
}