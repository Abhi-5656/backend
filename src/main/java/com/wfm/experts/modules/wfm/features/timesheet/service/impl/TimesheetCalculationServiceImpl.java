//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
//import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository.PayPolicyAssignmentRepository;
//import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
//import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
//import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
//import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
//import com.wfm.experts.setup.wfm.paypolicy.engine.executor.PayPolicyRuleExecutor;
//import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
//import com.wfm.experts.setup.wfm.paypolicy.repository.PayPolicyRepository;
//import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class TimesheetCalculationServiceImpl implements TimesheetCalculationService {
//
//    private final PayPolicyAssignmentRepository payPolicyAssignmentRepository;
//    private final PayPolicyRepository payPolicyRepository;
//    private final PunchEventRepository punchEventRepository;
//    private final TimesheetRepository timesheetRepository;
//    private final EmployeeShiftRepository employeeShiftRepository;
//    private final PayPolicyRuleExecutor payPolicyRuleExecutor;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    @Transactional
//    public void processPunchEvents(String employeeId, LocalDate date) {
//        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);
//
//        // Fetch punches regardless of whether a policy is assigned
//        LocalDateTime startOfDay = date.atStartOfDay();
//        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
//        List<PunchEvent> punches = punchEventRepository
//                .findAllByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);
//
//        // If there are no punches, there's nothing to calculate.
//        if (punches.isEmpty()) {
//            log.info("No punch events for employee: {} on date: {}. Clearing timesheet.", employeeId, date);
//            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList(), null, "Absent");
//            return;
//        }
//
//        PayPolicyAssignment assignment = payPolicyAssignmentRepository
//                .findActiveAssignment(employeeId, date)
//                .orElse(null);
//
//        // If no assignment, calculate from punches directly without rules
//        if (assignment == null) {
//            log.warn("No active PayPolicyAssignment found for employee: {} on {}. Calculating work duration from punches without rules.", employeeId, date);
//            int totalWorkMinutes = computeTotalWorkMinutes(punches, null); // Pass null for policy
//            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, Collections.emptyList(), null, "Present");
//            return;
//        }
//
//        PayPolicy policy = payPolicyRepository.findById(assignment.getPayPolicyId())
//                .orElse(null);
//
//        if (policy == null) {
//            log.error("PayPolicy {} not found for assignment {}. Calculating from punches, but rules will not be applied.", assignment.getPayPolicyId(), assignment.getId());
//            int totalWorkMinutes = computeTotalWorkMinutes(punches, null);
//            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, Collections.emptyList(), null, "Present");
//            return;
//        }
//
//
//        // --- Enrich Context ---
//        int totalWorkMinutes = computeTotalWorkMinutes(punches, policy); // Pass policy to calculation
//        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);
//        boolean isHoliday = false; // Placeholder for holiday check logic
//
//        Map<String, Object> facts = new HashMap<>();
//        facts.put("workedMinutes", totalWorkMinutes);
//        facts.put("shift", currentShift);
//        facts.put("isHoliday", isHoliday);
//
//        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
//                .employeeId(employeeId)
//                .date(date)
//                .payPolicy(policy)
//                .punchEvents(new ArrayList<>(punches)) // Use a mutable copy
//                .facts(facts)
//                .build();
//
//        List<PayPolicyRule> rules = policy.getRules();
//        List<PayPolicyRuleResultDTO> ruleResults = payPolicyRuleExecutor.executeRules(rules, context);
//
//        Integer finalWorkMinutes = (Integer) context.getFacts().getOrDefault("workedMinutes", totalWorkMinutes);
//        Integer overtimeMinutes = (Integer) context.getFacts().get("overtimeMinutes");
//
//        // Determine the final status from the AttendanceRule result
//        String finalStatus = ruleResults.stream()
//                .filter(r -> "AttendanceRule".equals(r.getRuleName()))
//                .map(PayPolicyRuleResultDTO::getResult)
//                .findFirst()
//                .orElse("Present"); // Default to "Present" if rule doesn't run or has no result
//
//        saveOrUpdateTimesheet(employeeId, date, finalWorkMinutes, ruleResults, overtimeMinutes, finalStatus);
//
//        log.info("Timesheet processed for employee: {} on date: {} ({} minutes, {} rules)",
//                employeeId, date, finalWorkMinutes, ruleResults.size());
//    }
//
//    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int workMinutes, List<PayPolicyRuleResultDTO> ruleResults, Integer overtime, String status) {
//        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
//                .orElseGet(() -> Timesheet.builder()
//                        .employeeId(employeeId)
//                        .workDate(date)
//                        .build());
//
//        timesheet.setWorkDurationMinutes(workMinutes);
//        timesheet.setTotalWorkDuration(workMinutes / 60.0);
//        timesheet.setOvertimeDuration(overtime);
//        timesheet.setStatus(status);
//        try {
//            timesheet.setRuleResultsJson(objectMapper.writeValueAsString(ruleResults));
//        } catch (JsonProcessingException e) {
//            log.error("Failed to serialize rule results for employee: {} on date: {}", employeeId, date, e);
//            timesheet.setRuleResultsJson("[]");
//        }
//        timesheet.setCalculatedAt(LocalDate.now());
//        timesheetRepository.save(timesheet);
//    }
//
//    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
//        if (punches == null || punches.isEmpty()) {
//            return 0;
//        }
//
//        // Check if FILO calculation should be used
//        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
//            log.debug("Using FILO calculation logic.");
//            Optional<LocalDateTime> firstIn = punches.stream()
//                    .filter(p -> p.getPunchType() == PunchType.IN)
//                    .map(PunchEvent::getEventTime)
//                    .min(LocalDateTime::compareTo);
//
//            Optional<LocalDateTime> lastOut = punches.stream()
//                    .filter(p -> p.getPunchType() == PunchType.OUT)
//                    .map(PunchEvent::getEventTime)
//                    .max(LocalDateTime::compareTo);
//
//            if (firstIn.isPresent() && lastOut.isPresent() && lastOut.get().isAfter(firstIn.get())) {
//                return (int) Duration.between(firstIn.get(), lastOut.get()).toMinutes();
//            } else {
//                return 0; // Not enough data for FILO
//            }
//        } else {
//            // Default logic: sum of durations between IN/OUT pairs
//            log.debug("Using standard paired punch calculation logic.");
//            punches.sort(Comparator.comparing(PunchEvent::getEventTime));
//            long totalMinutes = 0;
//            LocalDateTime inTime = null;
//
//            for (PunchEvent event : punches) {
//                if (event.getPunchType() == PunchType.IN) {
//                    if (inTime == null) {
//                        inTime = event.getEventTime();
//                    }
//                } else if (event.getPunchType() == PunchType.OUT) {
//                    if (inTime != null) {
//                        totalMinutes += Duration.between(inTime, event.getEventTime()).toMinutes();
//                        inTime = null;
//                    }
//                }
//            }
//            return (int) Math.max(0, totalMinutes);
//        }
//    }
//}
// src/main/java/com/wfm/experts/modules/wfm/features/timesheet/service/impl/TimesheetCalculationServiceImpl.java
// src/main/java/com/wfm/experts/modules/wfm/features/timesheet/service/impl/TimesheetCalculationServiceImpl.java
package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository.PayPolicyAssignmentRepository;
import com.wfm.experts.modules.wfm.features.roster.entity.EmployeeShift;
import com.wfm.experts.modules.wfm.features.roster.repository.EmployeeShiftRepository;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
import com.wfm.experts.modules.wfm.features.timesheet.enums.PunchType;
import com.wfm.experts.modules.wfm.features.timesheet.repository.PunchEventRepository;
import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
import com.wfm.experts.modules.wfm.features.timesheet.service.TimesheetCalculationService;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.engine.executor.PayPolicyRuleExecutor;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.paypolicy.repository.PayPolicyRepository;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetCalculationServiceImpl implements TimesheetCalculationService {

    private final PayPolicyAssignmentRepository payPolicyAssignmentRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final PunchEventRepository punchEventRepository;
    private final TimesheetRepository timesheetRepository;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final PayPolicyRuleExecutor payPolicyRuleExecutor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processPunchEvents(String employeeId, LocalDate date) {
        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);

        // Fetch punches regardless of whether a policy is assigned
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        List<PunchEvent> punches = punchEventRepository
                .findAllByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);

        // If there are no punches, there's nothing to calculate.
        if (punches.isEmpty()) {
            log.info("No punch events for employee: {} on date: {}. Clearing timesheet.", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, 0, 0, 0, 0, Collections.emptyList(), "Absent");
            return;
        }

        PayPolicyAssignment assignment = payPolicyAssignmentRepository
                .findActiveAssignment(employeeId, date)
                .orElse(null);

        // If no assignment, calculate from punches directly without rules
        if (assignment == null) {
            log.warn("No active PayPolicyAssignment found for employee: {} on {}. Calculating work duration from punches without rules.", employeeId, date);
            int totalWorkMinutes = computeTotalWorkMinutes(punches, null); // Pass null for policy
            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, 0, 0, 0, 0, Collections.emptyList(), "Present");
            return;
        }

        PayPolicy policy = payPolicyRepository.findById(assignment.getPayPolicyId())
                .orElse(null);

        if (policy == null) {
            log.error("PayPolicy {} not found for assignment {}. Calculating from punches, but rules will not be applied.", assignment.getPayPolicyId(), assignment.getId());
            int totalWorkMinutes = computeTotalWorkMinutes(punches, null);
            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, 0, 0, 0, 0, Collections.emptyList(), "Present");
            return;
        }


        // --- Enrich Context ---
        int totalWorkMinutes = computeTotalWorkMinutes(punches, policy); // Pass policy to calculation
        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);
        boolean isHoliday = false; // Placeholder for holiday check logic

        Map<String, Object> facts = new HashMap<>();
        facts.put("workedMinutes", totalWorkMinutes);
        facts.put("shift", currentShift);
        facts.put("isHoliday", isHoliday);

        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
                .employeeId(employeeId)
                .date(date)
                .payPolicy(policy)
                .punchEvents(new ArrayList<>(punches)) // Use a mutable copy
                .facts(facts)
                .timesheetRepository(timesheetRepository)
                .build();

        List<PayPolicyRule> rules = policy.getRules();
        List<PayPolicyRuleResultDTO> ruleResults = payPolicyRuleExecutor.executeRules(rules, context);

        Integer regularHoursMinutes = (Integer) context.getFacts().getOrDefault("regularHoursMinutes", 0);
        Integer dailyOtHoursMinutes = (Integer) context.getFacts().getOrDefault("dailyOtHoursMinutes", 0);
        Integer excessHoursMinutes = (Integer) context.getFacts().getOrDefault("excessHoursMinutes", 0);
        Integer weeklyOtHoursMinutes = (Integer) context.getFacts().getOrDefault("weeklyOtHoursMinutes", 0);


        // Determine the final status from the AttendanceRule result
        String finalStatus = ruleResults.stream()
                .filter(r -> "AttendanceRule".equals(r.getRuleName()))
                .map(PayPolicyRuleResultDTO::getResult)
                .findFirst()
                .orElse("Present"); // Default to "Present" if rule doesn't run or has no result

        saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, regularHoursMinutes, dailyOtHoursMinutes, excessHoursMinutes, weeklyOtHoursMinutes, ruleResults, finalStatus);

        log.info("Timesheet processed for employee: {} on date: {} ({} minutes, {} rules)",
                employeeId, date, totalWorkMinutes, ruleResults.size());
    }

    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int totalWorkMinutes, int regularHoursMinutes, int dailyOtHoursMinutes, int excessHoursMinutes, int weeklyOtHoursMinutes, List<PayPolicyRuleResultDTO> ruleResults, String status) {
        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
                .orElseGet(() -> Timesheet.builder()
                        .employeeId(employeeId)
                        .workDate(date)
                        .build());

        timesheet.setTotalWorkDurationMinutes(totalWorkMinutes);
        timesheet.setRegularHoursMinutes(regularHoursMinutes);
        timesheet.setDailyOtHoursMinutes(dailyOtHoursMinutes);
        timesheet.setExcessHoursMinutes(excessHoursMinutes);
        timesheet.setWeeklyOtHoursMinutes(weeklyOtHoursMinutes);
        timesheet.setStatus(status);
        try {
            timesheet.setRuleResultsJson(objectMapper.writeValueAsString(ruleResults));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rule results for employee: {} on date: {}", employeeId, date, e);
            timesheet.setRuleResultsJson("[]");
        }
        timesheet.setCalculatedAt(LocalDate.now());
        timesheetRepository.save(timesheet);
    }

    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
        if (punches == null || punches.isEmpty()) {
            return 0;
        }

        // Check if FILO calculation should be used
        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
            log.debug("Using FILO calculation logic.");
            Optional<LocalDateTime> firstIn = punches.stream()
                    .filter(p -> p.getPunchType() == PunchType.IN)
                    .map(PunchEvent::getEventTime)
                    .min(LocalDateTime::compareTo);

            Optional<LocalDateTime> lastOut = punches.stream()
                    .filter(p -> p.getPunchType() == PunchType.OUT)
                    .map(PunchEvent::getEventTime)
                    .max(LocalDateTime::compareTo);

            if (firstIn.isPresent() && lastOut.isPresent() && lastOut.get().isAfter(firstIn.get())) {
                return (int) Duration.between(firstIn.get(), lastOut.get()).toMinutes();
            } else {
                return 0; // Not enough data for FILO
            }
        } else {
            // Default logic: sum of durations between IN/OUT pairs
            log.debug("Using standard paired punch calculation logic.");
            punches.sort(Comparator.comparing(PunchEvent::getEventTime));
            long totalMinutes = 0;
            LocalDateTime inTime = null;

            for (PunchEvent event : punches) {
                if (event.getPunchType() == PunchType.IN) {
                    if (inTime == null) {
                        inTime = event.getEventTime();
                    }
                } else if (event.getPunchType() == PunchType.OUT) {
                    if (inTime != null) {
                        totalMinutes += Duration.between(inTime, event.getEventTime()).toMinutes();
                        inTime = null;
                    }
                }
            }
            return (int) Math.max(0, totalMinutes);
        }
    }
}