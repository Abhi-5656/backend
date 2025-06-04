package com.wfm.experts.modules.wfm.features.roster.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.repository.ShiftRotationAssignmentRepository;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotationDay;
import com.wfm.experts.setup.wfm.shift.enums.Weekday;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRotationDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final ShiftRotationAssignmentRepository assignmentRepository;
    private final ShiftRotationDayRepository shiftRotationDayRepository;
    private final EmployeeShiftRepository employeeShiftRepository;

    @Override
    public void generateShiftsFromRotation(String employeeId, LocalDate startDate, LocalDate endDate) {
        // 1. Fetch assignments in the window, sorted by effectiveDate
        List<ShiftRotationAssignment> allAssignments = assignmentRepository
                .findAllByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        if (allAssignments.isEmpty()) {
            throw new IllegalArgumentException("No shift rotation assignment found for employeeId: " + employeeId);
        }
        allAssignments.sort(Comparator.comparing(ShiftRotationAssignment::getEffectiveDate));

        // 2. Map LocalDate -> assignment
        Map<LocalDate, ShiftRotationAssignment> assignmentByDate = new HashMap<>();
        for (ShiftRotationAssignment assignment : allAssignments) {
            LocalDate eff = assignment.getEffectiveDate();
            LocalDate exp = assignment.getExpirationDate() != null ? assignment.getExpirationDate() : endDate;
            for (LocalDate d = eff; !d.isAfter(exp) && !d.isAfter(endDate); d = d.plusDays(1)) {
                if (!d.isBefore(startDate)) {
                    assignmentByDate.put(d, assignment); // later assignments override earlier
                }
            }
        }

        // 3. Fetch rotation days for all involved shift rotations
        Map<Long, List<ShiftRotationDay>> rotationDaysByRotationId = new HashMap<>();
        for (ShiftRotationAssignment assignment : allAssignments) {
            Long rotationId = assignment.getShiftRotation().getId();
            if (!rotationDaysByRotationId.containsKey(rotationId)) {
                List<ShiftRotationDay> days = shiftRotationDayRepository.findByShiftRotationId(rotationId);
                rotationDaysByRotationId.put(rotationId, days);
            }
        }

        // 4. Fetch existing (non-deleted) EmployeeShift records in range
        List<EmployeeShift> existingShifts = employeeShiftRepository
                .findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(employeeId, startDate, endDate);
        Map<LocalDate, EmployeeShift> shiftByDate = existingShifts.stream()
                .collect(Collectors.toMap(EmployeeShift::getCalendarDate, s -> s));

        List<EmployeeShift> toInsert = new ArrayList<>();
        List<EmployeeShift> toMarkDeleted = new ArrayList<>();

        // 5. For each date, mark old shift as deleted, insert new shift
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            ShiftRotationAssignment assignment = assignmentByDate.get(date);
            if (assignment == null) continue;

            int totalWeeks = assignment.getShiftRotation().getWeeks();
            List<ShiftRotationDay> rotationDays = rotationDaysByRotationId.get(assignment.getShiftRotation().getId());
            Map<String, ShiftRotationDay> rotationMap = rotationDays.stream()
                    .collect(Collectors.toMap(
                            d -> "W" + d.getWeek() + "_" + d.getWeekday().name(),
                            d -> d
                    ));

            int weekNumber = (int) ChronoUnit.WEEKS.between(assignment.getEffectiveDate(), date) % totalWeeks;
            Weekday weekdayEnum = Weekday.from(date.getDayOfWeek());
            String weekday = weekdayEnum.name();
            String key = "W" + (weekNumber + 1) + "_" + weekday;

            ShiftRotationDay rotationDay = rotationMap.get(key);

            Shift shift = (rotationDay != null && Boolean.TRUE.equals(rotationDay.getWeekOff())) ? null
                    : (rotationDay != null ? rotationDay.getShift() : null);
            boolean isWeekOff = rotationDay != null && Boolean.TRUE.equals(rotationDay.getWeekOff());
            boolean isHoliday = false; // Future: integrate with holiday service

            // If existing, mark as deleted, always insert new for audit/history
            EmployeeShift existing = shiftByDate.get(date);
            if (existing != null) {
                existing.setDeleted(true);
                toMarkDeleted.add(existing);
            }
            EmployeeShift empShift = EmployeeShift.builder()
                    .employeeId(employeeId)
                    .shift(shift)
                    .calendarDate(date)
                    .isWeekOff(isWeekOff)
                    .isHoliday(isHoliday)
                    .weekday(weekday)
                    .deleted(false)
                    .assignedBy("SYSTEM") // or set current user
                    .build();
            toInsert.add(empShift);
        }

        if (!toMarkDeleted.isEmpty()) employeeShiftRepository.saveAll(toMarkDeleted);
        if (!toInsert.isEmpty()) employeeShiftRepository.saveAll(toInsert);

        System.out.printf("Upserted %d shifts for employee %s from %s to %s%n",
                (toInsert.size() + toMarkDeleted.size()), employeeId, startDate, endDate);
    }

    @Override
    public List<EmployeeShiftDTO> getShiftsForEmployeeInRange(String employeeId, LocalDate startDate, LocalDate endDate) {
        return employeeShiftRepository
                .findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(employeeId, startDate, endDate)
                .stream()
                .map(shift -> EmployeeShiftDTO.builder()
                        .id(shift.getId())
                        .employeeId(shift.getEmployeeId())
                        .shift(shift.getShift() != null ? ShiftDTO.builder()
                                .id(shift.getShift().getId())
                                .shiftName(shift.getShift().getShiftName())
                                .shiftLabel(shift.getShift().getShiftLabel())
                                .color(shift.getShift().getColor())
                                .startTime(shift.getShift().getStartTime() != null ? shift.getShift().getStartTime().toString() : null)
                                .endTime(shift.getShift().getEndTime() != null ? shift.getShift().getEndTime().toString() : null)
                                .isActive(shift.getShift().getIsActive())
                                .build() : null)
                        .shiftName(shift.getShift() != null ? shift.getShift().getShiftName() : null)
                        .calendarDate(shift.getCalendarDate())
                        .isWeekOff(shift.getIsWeekOff())
                        .isHoliday(shift.getIsHoliday())
                        .weekday(shift.getWeekday())
                        .deleted(shift.getDeleted())
                        .assignedBy(shift.getAssignedBy())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteShift(Long shiftId) {
        EmployeeShift shift = employeeShiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        if (Boolean.TRUE.equals(shift.getDeleted())) {
            throw new IllegalStateException("Shift already deleted");
        }
        shift.setDeleted(true);
        employeeShiftRepository.save(shift);
    }

    @Override
    public void softUpdateShift(Long shiftId, String updatedBy) {
        // Mark current as deleted and add new one (clone fields, update assignedBy)
        EmployeeShift existing = employeeShiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        if (Boolean.TRUE.equals(existing.getDeleted())) {
            throw new IllegalStateException("Cannot update deleted shift");
        }
        existing.setDeleted(true);
        employeeShiftRepository.save(existing);

        EmployeeShift updated = EmployeeShift.builder()
                .employeeId(existing.getEmployeeId())
                .shift(existing.getShift())
                .calendarDate(existing.getCalendarDate())
                .isWeekOff(existing.getIsWeekOff())
                .isHoliday(existing.getIsHoliday())
                .weekday(existing.getWeekday())
                .deleted(false)
                .assignedBy(updatedBy)
                .build();
        employeeShiftRepository.save(updated);
    }
}
