package com.wfm.experts.modules.wfm.features.roster.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.entity.ShiftRotationAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.shiftrotation.repository.ShiftRotationAssignmentRepository;
import com.wfm.experts.modules.wfm.features.roster.dto.BulkEmployeeShiftUpdateRequestDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterDTO;
import com.wfm.experts.modules.wfm.features.roster.dto.EmployeeShiftRosterProjection;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.roster.service.EmployeeShiftService;
import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotationDay;
import com.wfm.experts.setup.wfm.shift.enums.Weekday;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRotationDayRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final ShiftRotationAssignmentRepository assignmentRepository;
    private final ShiftRotationDayRepository shiftRotationDayRepository;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final ShiftRepository shiftRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


//    @Override
//    public void generateShiftsFromRotation(String employeeId, LocalDate startDate, LocalDate endDate) {
//        // 1. Fetch assignments in the window, sorted by effectiveDate
//        List<ShiftRotationAssignment> allAssignments = assignmentRepository
//                .findAllByEmployeeIdAndDateRange(employeeId, startDate, endDate);
//        if (allAssignments.isEmpty()) {
//            throw new IllegalArgumentException("No shift rotation assignment found for employeeId: " + employeeId);
//        }
//        allAssignments.sort(Comparator.comparing(ShiftRotationAssignment::getEffectiveDate));
//
//        // 2. Map LocalDate -> assignment
//        Map<LocalDate, ShiftRotationAssignment> assignmentByDate = new HashMap<>();
//        for (ShiftRotationAssignment assignment : allAssignments) {
//            LocalDate eff = assignment.getEffectiveDate();
//            LocalDate exp = assignment.getExpirationDate() != null ? assignment.getExpirationDate() : endDate;
//            for (LocalDate d = eff; !d.isAfter(exp) && !d.isAfter(endDate); d = d.plusDays(1)) {
//                if (!d.isBefore(startDate)) {
//                    assignmentByDate.put(d, assignment); // later assignments override earlier
//                }
//            }
//        }
//
//        // 3. Fetch rotation days for all involved shift rotations
//        Map<Long, List<ShiftRotationDay>> rotationDaysByRotationId = new HashMap<>();
//        for (ShiftRotationAssignment assignment : allAssignments) {
//            Long rotationId = assignment.getShiftRotation().getId();
//            if (!rotationDaysByRotationId.containsKey(rotationId)) {
//                List<ShiftRotationDay> days = shiftRotationDayRepository.findByShiftRotationId(rotationId);
//                rotationDaysByRotationId.put(rotationId, days);
//            }
//        }
//
//        // 4. Fetch existing (non-deleted) EmployeeShift records in range
//        List<EmployeeShift> existingShifts = employeeShiftRepository
//                .findByEmployeeIdAndCalendarDateBetweenAndDeletedFalse(employeeId, startDate, endDate);
//        Map<LocalDate, EmployeeShift> shiftByDate = existingShifts.stream()
//                .collect(Collectors.toMap(EmployeeShift::getCalendarDate, s -> s));
//
//        List<EmployeeShift> toInsert = new ArrayList<>();
//        List<EmployeeShift> toMarkDeleted = new ArrayList<>();
//
//        // 5. For each date, mark old shift as deleted, insert new shift
//        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//            ShiftRotationAssignment assignment = assignmentByDate.get(date);
//            if (assignment == null) continue;
//
//            int totalWeeks = assignment.getShiftRotation().getWeeks();
//            List<ShiftRotationDay> rotationDays = rotationDaysByRotationId.get(assignment.getShiftRotation().getId());
//            Map<String, ShiftRotationDay> rotationMap = rotationDays.stream()
//                    .collect(Collectors.toMap(
//                            d -> "W" + d.getWeek() + "_" + d.getWeekday().name(),
//                            d -> d
//                    ));
//
//            int weekNumber = (int) ChronoUnit.WEEKS.between(assignment.getEffectiveDate(), date) % totalWeeks;
//            Weekday weekdayEnum = Weekday.from(date.getDayOfWeek());
//            String weekday = weekdayEnum.name();
//            String key = "W" + (weekNumber + 1) + "_" + weekday;
//
//            ShiftRotationDay rotationDay = rotationMap.get(key);
//
//            Shift shift = (rotationDay != null && Boolean.TRUE.equals(rotationDay.getWeekOff())) ? null
//                    : (rotationDay != null ? rotationDay.getShift() : null);
//            boolean isWeekOff = rotationDay != null && Boolean.TRUE.equals(rotationDay.getWeekOff());
//            boolean isHoliday = false; // Future: integrate with holiday service
//
//            // If existing, mark as deleted, always insert new for audit/history
//            EmployeeShift existing = shiftByDate.get(date);
//            if (existing != null) {
//                existing.setDeleted(true);
//                toMarkDeleted.add(existing);
//            }
//            EmployeeShift empShift = EmployeeShift.builder()
//                    .employeeId(employeeId)
//                    .shift(shift)
//                    .calendarDate(date)
//                    .isWeekOff(isWeekOff)
//                    .isHoliday(isHoliday)
//                    .weekday(weekday)
//                    .deleted(false)
//                    .assignedBy("SYSTEM") // or set current user
//                    .build();
//            toInsert.add(empShift);
//        }
//
//        if (!toMarkDeleted.isEmpty()) employeeShiftRepository.saveAll(toMarkDeleted);
//        if (!toInsert.isEmpty()) employeeShiftRepository.saveAll(toInsert);
//
//        System.out.printf("Upserted %d shifts for employee %s from %s to %s%n",
//                (toInsert.size() + toMarkDeleted.size()), employeeId, startDate, endDate);
//    }
@Override
public void generateShiftsFromRotation(List<String> employeeIds, LocalDate startDate, LocalDate endDate) {
    if (employeeIds == null || employeeIds.isEmpty()) {
        throw new IllegalArgumentException("No employee IDs provided");
    }

    // 1. Fetch assignments for all employees
    Map<String, List<ShiftRotationAssignment>> assignmentsByEmployee = new HashMap<>();
    for (String employeeId : employeeIds) {
        List<ShiftRotationAssignment> assignments = assignmentRepository
                .findAllByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        if (assignments.isEmpty()) continue;
        assignments.sort(Comparator.comparing(ShiftRotationAssignment::getEffectiveDate));
        assignmentsByEmployee.put(employeeId, assignments);
    }

    if (assignmentsByEmployee.isEmpty()) {
        throw new IllegalArgumentException("No shift rotation assignments found for any employee");
    }

    // 2. Map employeeId -> (date -> assignment)
    Map<String, Map<LocalDate, ShiftRotationAssignment>> assignmentByEmpDate = new HashMap<>();
    for (String employeeId : assignmentsByEmployee.keySet()) {
        Map<LocalDate, ShiftRotationAssignment> assignmentByDate = new HashMap<>();
        for (ShiftRotationAssignment assignment : assignmentsByEmployee.get(employeeId)) {
            LocalDate eff = assignment.getEffectiveDate();
            LocalDate exp = assignment.getExpirationDate() != null ? assignment.getExpirationDate() : endDate;
            for (LocalDate d = eff; !d.isAfter(exp) && !d.isAfter(endDate); d = d.plusDays(1)) {
                if (!d.isBefore(startDate)) {
                    assignmentByDate.put(d, assignment); // later assignments override earlier
                }
            }
        }
        assignmentByEmpDate.put(employeeId, assignmentByDate);
    }

    // 3. Collect all unique shift rotation IDs
    Set<Long> allRotationIds = assignmentsByEmployee.values().stream()
            .flatMap(List::stream)
            .map(a -> a.getShiftRotation().getId())
            .collect(Collectors.toSet());

    // 4. Preload rotation days for all shift rotations
    Map<Long, List<ShiftRotationDay>> rotationDaysByRotationId = new HashMap<>();
    for (Long rotationId : allRotationIds) {
        List<ShiftRotationDay> days = shiftRotationDayRepository.findByShiftRotationId(rotationId);
        rotationDaysByRotationId.put(rotationId, days);
    }

    // 5. Fetch existing (non-deleted) EmployeeShift records for all employees in range
    List<EmployeeShift> existingShifts = employeeShiftRepository
            .findByEmployeeIdInAndCalendarDateBetweenAndDeletedFalse(employeeIds, startDate, endDate);
    Map<String, Map<LocalDate, EmployeeShift>> shiftByEmpDate = existingShifts.stream()
            .collect(Collectors.groupingBy(EmployeeShift::getEmployeeId,
                    Collectors.toMap(EmployeeShift::getCalendarDate, s -> s)));

    List<EmployeeShift> toInsert = new ArrayList<>();
    List<EmployeeShift> toMarkDeleted = new ArrayList<>();

    // 6. For each employee, for each date
    for (String employeeId : employeeIds) {
        Map<LocalDate, ShiftRotationAssignment> assignmentByDate = assignmentByEmpDate.get(employeeId);
        if (assignmentByDate == null) continue;

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
            boolean isHoliday = false; // Integrate holiday logic if needed

            // Existing shift handling
            EmployeeShift existing = shiftByEmpDate.getOrDefault(employeeId, Collections.emptyMap()).get(date);
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
    }

    if (!toMarkDeleted.isEmpty()) employeeShiftRepository.saveAll(toMarkDeleted);
    if (!toInsert.isEmpty()) employeeShiftRepository.saveAll(toInsert);

    System.out.printf("Upserted %d shifts for employees %s from %s to %s%n",
            (toInsert.size() + toMarkDeleted.size()), employeeIds, startDate, endDate);
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


    @Override
    public List<EmployeeShiftRosterDTO> getEmployeeRosterForDateRange(LocalDate startDate, LocalDate endDate) {
        List<EmployeeShiftRosterProjection> rows =
                employeeShiftRepository.findEmployeeShiftRosterWithShiftDetails(startDate, endDate);

        return rows.stream().map(row -> {
            ShiftDTO shift = null;
            if (row.getShiftId() != null) {
                shift = ShiftDTO.builder()
                        .id(row.getShiftId())
                        .shiftName(row.getShiftName())
                        .startTime(row.getShiftStartTime() != null ? row.getShiftStartTime().format(TIME_FORMATTER) : null)
                        .endTime(row.getShiftEndTime() != null ? row.getShiftEndTime().format(TIME_FORMATTER) : null)
                        .color(row.getShiftColor())
                        .build();
            }
            return EmployeeShiftRosterDTO.builder()
                    .employeeId(row.getEmployeeId())
                    .fullName(row.getFullName())
                    .calendarDate(row.getCalendarDate())
                    .shift(shift)
                    .isWeekOff(row.getIsWeekOff() != null ? row.getIsWeekOff() : false)   // Default to false if null
                    .isHoliday(row.getIsHoliday() != null ? row.getIsHoliday() : false)   // Default to false if null
                    .weekday(row.getWeekday() != null ? row.getWeekday() : deriveWeekday(row.getCalendarDate())) // Optional: derive from date
                    .deleted(row.getDeleted() != null ? row.getDeleted() : false)
                    .assignedBy(row.getAssignedBy())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Helper method to derive weekday string from a LocalDate.
     * E.g., "MONDAY", "TUESDAY", etc.
     */
    private String deriveWeekday(LocalDate date) {
        return date != null ? date.getDayOfWeek().name() : null;
    }

    @Override
    @Transactional
    public void bulkAssignOrUpdateShifts(BulkEmployeeShiftUpdateRequestDTO request) {
        // Fetch the Shift entity (if provided)
        Shift shift = null;
        if (request.getShiftId() != null) {
            shift = shiftRepository.findById(request.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Shift not found: " + request.getShiftId()));
        }
        String assignedBy = request.getAssignedBy();
        List<String> employeeIds = request.getEmployeeIds();
        List<LocalDate> calendarDates = request.getCalendarDates();

        // Fetch all existing (not deleted) assignments for these employees/dates in one go
        List<EmployeeShift> existingAssignments =
                employeeShiftRepository.findByEmployeeIdInAndCalendarDateBetweenAndDeletedFalse(
                        employeeIds,
                        Collections.min(calendarDates),
                        Collections.max(calendarDates)
                );

        // Build a map for quick lookup: (employeeId + calendarDate) -> EmployeeShift
        Map<String, EmployeeShift> existingMap = new HashMap<>();
        for (EmployeeShift es : existingAssignments) {
            String key = es.getEmployeeId() + "|" + es.getCalendarDate();
            existingMap.put(key, es);
        }

        List<EmployeeShift> toSave = new ArrayList<>();

        // Iterate over every (employee, date) combination
        for (String employeeId : employeeIds) {
            for (LocalDate calendarDate : calendarDates) {
                String key = employeeId + "|" + calendarDate;
                EmployeeShift previous = existingMap.get(key);

                if (previous != null) {
                    // 1. Soft-delete the old assignment (set deleted = true)
                    previous.setDeleted(true);
                    employeeShiftRepository.save(previous);
                }

                // 2. Insert new (current) assignment
                EmployeeShift newAssignment = EmployeeShift.builder()
                        .employeeId(employeeId)
                        .shift(shift)
                        .calendarDate(calendarDate)
                        .isWeekOff(false)
                        .isHoliday(false)
                        .weekday(calendarDate.getDayOfWeek().name())
                        .deleted(false)
                        .assignedBy(assignedBy)
                        .build();
                toSave.add(newAssignment);
            }
        }

        // Save all new assignments in batch
        employeeShiftRepository.saveAll(toSave);
    }



}
