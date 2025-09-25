//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
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
//import com.wfm.experts.setup.wfm.paypolicy.entity.RoundingRules;
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
//    private final HolidayProfileAssignmentService holidayProfileAssignmentService;
//
//    @Override
//    @Transactional
//    public void processPunchEvents(String employeeId, LocalDate date) {
//        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);
//
//        LocalDateTime startWindow = date.atStartOfDay().minusHours(12);
//        LocalDateTime endWindow = date.atStartOfDay().plusHours(36);
//        List<PunchEvent> punchesInWindow = punchEventRepository.findAllByEmployeeIdAndEventTimeBetween(employeeId, startWindow, endWindow);
//
//        WorkSession workSession = findWorkSessionForDate(punchesInWindow, date);
//        if (workSession == null) {
//            saveOrUpdateTimesheet(employeeId, date, 0, 0, 0, Collections.emptyList(), "Absent");
//            return;
//        }
//
//        int grossTotalWorkMinutes = workSession.getTotalMinutes();
//
//        PayPolicyAssignment assignment = payPolicyAssignmentRepository.findActiveAssignment(employeeId, date).orElse(null);
//        PayPolicy policy = (assignment != null) ? payPolicyRepository.findById(assignment.getPayPolicyId()).orElse(null) : null;
//        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);
//
//        PayPolicyExecutionContext context = buildAndExecuteRules(employeeId, date, workSession, policy, currentShift);
//
//        // --- Final Consolidation ---
//        int finalRegularMinutes = (int) context.getFacts().getOrDefault("finalRegularMinutes", 0);
//        int finalExcessHours = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
//
//        int totalExcessForDto;
//
//        if (policy != null) {
//            // When a policy is applied, strictly use the value from the ExcessHoursRule.
//            totalExcessForDto = finalExcessHours;
//        } else {
//            // When no policy is applied, the logic in buildAndExecuteRules calculates excess differently.
//            // We retrieve it from the same fact for consistency.
//            totalExcessForDto = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
//        }
//
//        // On holidays or weekends, all time becomes excess time.
//        if ((boolean) context.getFacts().get("isHoliday") || (boolean) context.getFacts().get("isWeekend")) {
//            // Note: This logic now operates on the already determined excess value.
//            // For a complete implementation, you might want to add dailyOtMinutes here as well.
//            totalExcessForDto += finalRegularMinutes;
//            finalRegularMinutes = 0;
//        }
//
//        List<PayPolicyRuleResultDTO> ruleResults = (List<PayPolicyRuleResultDTO>) context.getFacts().get("ruleResults");
//        String finalStatus = ruleResults.stream().filter(r -> "AttendanceRule".equals(r.getRuleName())).map(PayPolicyRuleResultDTO::getResult).findFirst().orElse("Present");
//
//        saveOrUpdateTimesheet(employeeId, date, grossTotalWorkMinutes, finalRegularMinutes, totalExcessForDto, ruleResults, finalStatus);
//    }
//
//    private PayPolicyExecutionContext buildAndExecuteRules(String employeeId, LocalDate date, WorkSession workSession, PayPolicy policy, EmployeeShift currentShift) {
//        int initialWorkMinutes = workSession.getTotalMinutes();
//        List<PunchEvent> relevantPunches = workSession.getPunchEvents();
//
//        boolean isHoliday = holidayProfileAssignmentService.getAssignedHolidaysByEmployeeId(employeeId).stream()
//                .anyMatch(holiday -> !date.isBefore(holiday.getStartDate()) && !date.isAfter(holiday.getEndDate()));
//
//        Map<String, Object> facts = new HashMap<>();
//        facts.put("shift", currentShift);
//        facts.put("isHoliday", isHoliday);
//        facts.put("isWeekend", false);
//        facts.put("grossWorkMinutes", initialWorkMinutes);
//
//        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
//                .employeeId(employeeId).date(date).payPolicy(policy)
//                .punchEvents(relevantPunches).facts(facts)
//                .timesheetRepository(timesheetRepository).build();
//
//        List<PayPolicyRuleResultDTO> ruleResults = new ArrayList<>();
//        context.getFacts().put("ruleResults", ruleResults);
//
//        if (policy == null) {
//            facts.put("workedMinutes", initialWorkMinutes);
//
//            if (currentShift != null && currentShift.getShift() != null) {
//                long shiftDuration = Duration.between(currentShift.getShift().getStartTime(), currentShift.getShift().getEndTime()).toMinutes();
//                if (shiftDuration < 0) {
//                    shiftDuration += 1440;
//                }
//
//                int regularMinutes = Math.min(initialWorkMinutes, (int) shiftDuration);
//                int excessMinutes = Math.max(0, initialWorkMinutes - (int) shiftDuration);
//
//                facts.put("finalRegularMinutes", regularMinutes);
//                facts.put("excessHoursMinutes", excessMinutes);
//            } else {
//                facts.put("finalRegularMinutes", initialWorkMinutes);
//                facts.put("excessHoursMinutes", 0);
//            }
//
//            return context;
//        }
//
//        List<PayPolicyRule> rules = policy.getRules();
//
//        rules.stream().filter(r -> r instanceof RoundingRules).findFirst().ifPresent(rule -> {
//            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
//        });
//
//        int roundedWorkMinutes = computeTotalWorkMinutes(context.getPunchEvents(), policy);
//        facts.put("workedMinutes", roundedWorkMinutes);
//
//        rules.stream().filter(r -> !(r instanceof RoundingRules)).forEach(rule -> {
//            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
//        });
//
//        return context;
//    }
//
//    private WorkSession findWorkSessionForDate(List<PunchEvent> punchesInWindow, LocalDate workDate) {
//        if (punchesInWindow == null || punchesInWindow.isEmpty()) return null;
//        punchesInWindow.sort(Comparator.comparing(PunchEvent::getEventTime));
//        for (int i = 0; i < punchesInWindow.size(); i++) {
//            if (punchesInWindow.get(i).getPunchType() == PunchType.IN && punchesInWindow.get(i).getEventTime().toLocalDate().isEqual(workDate)) {
//                for (int j = i + 1; j < punchesInWindow.size(); j++) {
//                    if (punchesInWindow.get(j).getPunchType() == PunchType.OUT) {
//                        return new WorkSession(punchesInWindow.subList(i, j + 1));
//                    }
//                }
//                break;
//            }
//        }
//        return null;
//    }
//
//    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
//        if (punches == null || punches.isEmpty()) return 0;
//        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
//        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
//            LocalDateTime firstIn = punches.get(0).getEventTime();
//            LocalDateTime lastOut = punches.get(punches.size() - 1).getEventTime();
//            return (int) Duration.between(firstIn, lastOut).toMinutes();
//        } else {
//            long totalMinutes = 0;
//            LocalDateTime inTime = null;
//            for (PunchEvent event : punches) {
//                if (event.getPunchType() == PunchType.IN) {
//                    if (inTime == null) inTime = event.getEventTime();
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
//
//    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int totalWorkMinutes, int regularHoursMinutes, int excessHoursMinutes, List<PayPolicyRuleResultDTO> ruleResults, String status) {
//        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
//                .orElseGet(() -> Timesheet.builder().employeeId(employeeId).workDate(date).build());
//        timesheet.setTotalWorkDurationMinutes(totalWorkMinutes);
//        timesheet.setRegularHoursMinutes(regularHoursMinutes);
//        timesheet.setExcessHoursMinutes(excessHoursMinutes);
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
//    private class WorkSession {
//        private final List<PunchEvent> punches;
//        private final int totalMinutes;
//
//        WorkSession(List<PunchEvent> punches) {
//            this.punches = punches;
//            this.totalMinutes = computeTotalWorkMinutes(punches, null);
//        }
//
//        public List<PunchEvent> getPunchEvents() { return punches; }
//        public int getTotalMinutes() { return totalMinutes; }
//    }
//}
// src/main/java/com/wfm/experts/modules/wfm/features/timesheet/service/impl/TimesheetCalculationServiceImpl.java
// src/main/java/com/wfm/experts/modules/wfm/features/timesheet/service/impl/TimesheetCalculationServiceImpl.java
// src/main/java/com/wfm/experts/modules/wfm/features/timesheet/service/impl/TimesheetCalculationServiceImpl.java
//package com.wfm.experts.modules.wfm.features.timesheet.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
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
//import com.wfm.experts.setup.wfm.paypolicy.entity.RoundingRules;
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
//import java.util.stream.Collectors;
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
//    private final HolidayProfileAssignmentService holidayProfileAssignmentService;
//
//    @Override
//    @Transactional
//    public void processPunchEvents(String employeeId, LocalDate date) {
//        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);
//
//        LocalDateTime startWindow = date.atStartOfDay().minusHours(12);
//        LocalDateTime endWindow = date.atStartOfDay().plusHours(36);
//        List<PunchEvent> punchesInWindow = punchEventRepository.findAllByEmployeeIdAndEventTimeBetween(employeeId, startWindow, endWindow);
//
//        WorkSession workSession = findWorkSessionForDate(punchesInWindow, date);
//        if (workSession == null) {
//            saveOrUpdateTimesheet(employeeId, date, 0, 0, 0, Collections.emptyList(), "Absent");
//            return;
//        }
//
//        PayPolicyAssignment assignment = payPolicyAssignmentRepository.findActiveAssignment(employeeId, date).orElse(null);
//        PayPolicy policy = (assignment != null) ? payPolicyRepository.findById(assignment.getPayPolicyId()).orElse(null) : null;
//
//        int grossTotalWorkMinutes = computeTotalWorkMinutes(workSession.getPunchEvents(), policy);
//
//        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);
//
//        PayPolicyExecutionContext context = buildAndExecuteRules(employeeId, date, workSession, policy, currentShift);
//
//        // --- Final Consolidation ---
//        int finalRegularMinutes = (int) context.getFacts().getOrDefault("finalRegularMinutes", 0);
//        int finalExcessHours = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
//
//        int totalExcessForDto;
//
//        if (policy != null) {
//            // When a policy is applied, strictly use the value from the ExcessHoursRule.
//            totalExcessForDto = finalExcessHours;
//        } else {
//            // When no policy is applied, the logic in buildAndExecuteRules calculates excess differently.
//            // We retrieve it from the same fact for consistency.
//            totalExcessForDto = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
//        }
//
//        // On holidays or weekends, all time becomes excess time.
//        if ((boolean) context.getFacts().get("isHoliday") || (boolean) context.getFacts().get("isWeekend")) {
//            // Note: This logic now operates on the already determined excess value.
//            // For a complete implementation, you might want to add dailyOtMinutes here as well.
//            totalExcessForDto += finalRegularMinutes;
//            finalRegularMinutes = 0;
//        }
//
//        List<PayPolicyRuleResultDTO> ruleResults = (List<PayPolicyRuleResultDTO>) context.getFacts().get("ruleResults");
//        String finalStatus = ruleResults.stream().filter(r -> "AttendanceRule".equals(r.getRuleName())).map(PayPolicyRuleResultDTO::getResult).findFirst().orElse("Present");
//
//        saveOrUpdateTimesheet(employeeId, date, grossTotalWorkMinutes, finalRegularMinutes, totalExcessForDto, ruleResults, finalStatus);
//    }
//
//    private PayPolicyExecutionContext buildAndExecuteRules(String employeeId, LocalDate date, WorkSession workSession, PayPolicy policy, EmployeeShift currentShift) {
//        int initialWorkMinutes = workSession.getTotalMinutes();
//        List<PunchEvent> relevantPunches = workSession.getPunchEvents();
//
//        boolean isHoliday = holidayProfileAssignmentService.getAssignedHolidaysByEmployeeId(employeeId).stream()
//                .anyMatch(holiday -> !date.isBefore(holiday.getStartDate()) && !date.isAfter(holiday.getEndDate()));
//
//        Map<String, Object> facts = new HashMap<>();
//        facts.put("shift", currentShift);
//        facts.put("isHoliday", isHoliday);
//        facts.put("isWeekend", false);
//        facts.put("grossWorkMinutes", initialWorkMinutes);
//
//        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
//                .employeeId(employeeId).date(date).payPolicy(policy)
//                .punchEvents(relevantPunches).facts(facts)
//                .timesheetRepository(timesheetRepository).build();
//
//        List<PayPolicyRuleResultDTO> ruleResults = new ArrayList<>();
//        context.getFacts().put("ruleResults", ruleResults);
//
//        if (policy == null) {
//            facts.put("workedMinutes", initialWorkMinutes);
//
//            if (currentShift != null && currentShift.getShift() != null) {
//                long shiftDuration = Duration.between(currentShift.getShift().getStartTime(), currentShift.getShift().getEndTime()).toMinutes();
//                if (shiftDuration < 0) {
//                    shiftDuration += 1440;
//                }
//
//                int regularMinutes = Math.min(initialWorkMinutes, (int) shiftDuration);
//                int excessMinutes = Math.max(0, initialWorkMinutes - (int) shiftDuration);
//
//                facts.put("finalRegularMinutes", regularMinutes);
//                facts.put("excessHoursMinutes", excessMinutes);
//            } else {
//                facts.put("finalRegularMinutes", initialWorkMinutes);
//                facts.put("excessHoursMinutes", 0);
//            }
//
//            return context;
//        }
//
//        List<PayPolicyRule> rules = policy.getRules();
//
//        rules.stream().filter(r -> r instanceof RoundingRules).findFirst().ifPresent(rule -> {
//            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
//        });
//
//        int roundedWorkMinutes = computeTotalWorkMinutes(context.getPunchEvents(), policy);
//        facts.put("workedMinutes", roundedWorkMinutes);
//
//        rules.stream().filter(r -> !(r instanceof RoundingRules)).forEach(rule -> {
//            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
//        });
//
//        return context;
//    }
//
//    private WorkSession findWorkSessionForDate(List<PunchEvent> punchesInWindow, LocalDate workDate) {
//        if (punchesInWindow == null || punchesInWindow.isEmpty()) return null;
//        punchesInWindow.sort(Comparator.comparing(PunchEvent::getEventTime));
//        for (int i = 0; i < punchesInWindow.size(); i++) {
//            if (punchesInWindow.get(i).getPunchType() == PunchType.IN && punchesInWindow.get(i).getEventTime().toLocalDate().isEqual(workDate)) {
//                // Found the first IN punch for the day.
//                // Now, find the last OUT punch after this IN punch.
//                int lastOutIndex = -1;
//                for (int j = i + 1; j < punchesInWindow.size(); j++) {
//                    if (punchesInWindow.get(j).getPunchType() == PunchType.OUT) {
//                        lastOutIndex = j;
//                    }
//                }
//
//                if (lastOutIndex != -1) {
//                    // Both IN and OUT found
//                    return new WorkSession(punchesInWindow.subList(i, lastOutIndex + 1));
//                } else {
//                    // Only IN found, return the rest of the punches from that point
//                    return new WorkSession(punchesInWindow.subList(i, punchesInWindow.size()));
//                }
//            }
//        }
//        return null;
//    }
//
//    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
//        if (punches == null || punches.isEmpty()) return 0;
//        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
//
//        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
//            Optional<PunchEvent> firstIn = punches.stream()
//                    .filter(p -> p.getPunchType() == PunchType.IN)
//                    .findFirst();
//
//            Optional<PunchEvent> lastOut = punches.stream()
//                    .filter(p -> p.getPunchType() == PunchType.OUT)
//                    .reduce((first, second) -> second);
//
//            if (firstIn.isPresent() && lastOut.isPresent()) {
//                return (int) Duration.between(firstIn.get().getEventTime(), lastOut.get().getEventTime()).toMinutes();
//            } else {
//                return 0; // Incomplete punch, so work duration is 0 for calculation purposes
//            }
//        } else {
//            long totalMinutes = 0;
//            LocalDateTime inTime = null;
//            for (PunchEvent event : punches) {
//                if (event.getPunchType() == PunchType.IN) {
//                    if (inTime == null) inTime = event.getEventTime();
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
//
//    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int totalWorkMinutes, int regularHoursMinutes, int excessHoursMinutes, List<PayPolicyRuleResultDTO> ruleResults, String status) {
//        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
//                .orElseGet(() -> Timesheet.builder().employeeId(employeeId).workDate(date).build());
//        timesheet.setTotalWorkDurationMinutes(totalWorkMinutes);
//        timesheet.setRegularHoursMinutes(regularHoursMinutes);
//        timesheet.setExcessHoursMinutes(excessHoursMinutes);
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
//    private class WorkSession {
//        private final List<PunchEvent> punches;
//        private final int totalMinutes;
//
//        WorkSession(List<PunchEvent> punches) {
//            this.punches = punches;
//            this.totalMinutes = computeTotalWorkMinutes(punches, null);
//        }
//
//        public List<PunchEvent> getPunchEvents() { return punches; }
//        public int getTotalMinutes() { return totalMinutes; }
//    }
//}
package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.modules.wfm.employee.assignment.holidayprofile.service.HolidayProfileAssignmentService;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
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
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyRuleResultDTO;
import com.wfm.experts.setup.wfm.paypolicy.engine.context.PayPolicyExecutionContext;
import com.wfm.experts.setup.wfm.paypolicy.engine.executor.PayPolicyRuleExecutor;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.paypolicy.entity.RoundingRules;
import com.wfm.experts.setup.wfm.paypolicy.repository.PayPolicyRepository;
import com.wfm.experts.setup.wfm.paypolicy.rule.PayPolicyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private final HolidayProfileAssignmentService holidayProfileAssignmentService;
    private final LeaveAccrualService leaveAccrualService;
    private final LeaveProfileAssignmentRepository leaveProfileAssignmentRepository;
    private final LeaveProfileRepository leaveProfileRepository;


    @Override
    @Transactional
    public void processPunchEvents(String employeeId, LocalDate date) {
        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);

        LocalDateTime startWindow = date.atStartOfDay().minusHours(12);
        LocalDateTime endWindow = date.atStartOfDay().plusHours(36);
        List<PunchEvent> punchesInWindow = punchEventRepository.findAllByEmployeeIdAndEventTimeBetween(employeeId, startWindow, endWindow);

        WorkSession workSession = findWorkSessionForDate(punchesInWindow, date);
        if (workSession == null) {
            saveOrUpdateTimesheet(employeeId, date, 0, 0, 0, Collections.emptyList(), "Absent");
            return;
        }

        PayPolicyAssignment assignment = payPolicyAssignmentRepository.findActiveAssignment(employeeId, date).orElse(null);
        PayPolicy policy = (assignment != null) ? payPolicyRepository.findById(assignment.getPayPolicyId()).orElse(null) : null;

        int grossTotalWorkMinutes = computeTotalWorkMinutes(workSession.getPunchEvents(), policy);

        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);

        PayPolicyExecutionContext context = buildAndExecuteRules(employeeId, date, workSession, policy, currentShift);

        // --- Final Consolidation ---
        int finalRegularMinutes = (int) context.getFacts().getOrDefault("finalRegularMinutes", 0);
        int finalExcessHours = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);

        int totalExcessForDto;

        if (policy != null) {
            totalExcessForDto = finalExcessHours;
        } else {
            totalExcessForDto = (int) context.getFacts().getOrDefault("excessHoursMinutes", 0);
        }

        if ((boolean) context.getFacts().get("isHoliday") || (boolean) context.getFacts().get("isWeekend")) {
            totalExcessForDto += finalRegularMinutes;
            finalRegularMinutes = 0;
        }

        List<PayPolicyRuleResultDTO> ruleResults = (List<PayPolicyRuleResultDTO>) context.getFacts().get("ruleResults");
        String finalStatus = ruleResults.stream().filter(r -> "AttendanceRule".equals(r.getRuleName())).map(PayPolicyRuleResultDTO::getResult).findFirst().orElse("Present");

        saveOrUpdateTimesheet(employeeId, date, grossTotalWorkMinutes, finalRegularMinutes, totalExcessForDto, ruleResults, finalStatus);

        YearMonth punchMonth = YearMonth.from(date);
        YearMonth currentMonth = YearMonth.now();

        if (punchMonth.isBefore(currentMonth)) {
            leaveAccrualService.recalculateTotalLeaveBalance(employeeId);
        }
    }

    private PayPolicyExecutionContext buildAndExecuteRules(String employeeId, LocalDate date, WorkSession workSession, PayPolicy policy, EmployeeShift currentShift) {
        int initialWorkMinutes = workSession.getTotalMinutes();
        List<PunchEvent> relevantPunches = workSession.getPunchEvents();

        boolean isHoliday = holidayProfileAssignmentService.getAssignedHolidaysByEmployeeId(employeeId).stream()
                .anyMatch(holiday -> !date.isBefore(holiday.getStartDate()) && !date.isAfter(holiday.getEndDate()));

        Map<String, Object> facts = new HashMap<>();
        facts.put("shift", currentShift);
        facts.put("isHoliday", isHoliday);
        facts.put("isWeekend", false);
        facts.put("grossWorkMinutes", initialWorkMinutes);

        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
                .employeeId(employeeId).date(date).payPolicy(policy)
                .punchEvents(relevantPunches).facts(facts)
                .timesheetRepository(timesheetRepository).build();

        List<PayPolicyRuleResultDTO> ruleResults = new ArrayList<>();
        context.getFacts().put("ruleResults", ruleResults);

        if (policy == null) {
            facts.put("workedMinutes", initialWorkMinutes);

            if (currentShift != null && currentShift.getShift() != null) {
                long shiftDuration = Duration.between(currentShift.getShift().getStartTime(), currentShift.getShift().getEndTime()).toMinutes();
                if (shiftDuration < 0) {
                    shiftDuration += 1440;
                }

                int regularMinutes = Math.min(initialWorkMinutes, (int) shiftDuration);
                int excessMinutes = Math.max(0, initialWorkMinutes - (int) shiftDuration);

                facts.put("finalRegularMinutes", regularMinutes);
                facts.put("excessHoursMinutes", excessMinutes);
            } else {
                facts.put("finalRegularMinutes", initialWorkMinutes);
                facts.put("excessHoursMinutes", 0);
            }

            return context;
        }

        List<PayPolicyRule> rules = policy.getRules();

        rules.stream().filter(r -> r instanceof RoundingRules).findFirst().ifPresent(rule -> {
            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
        });

        int roundedWorkMinutes = computeTotalWorkMinutes(context.getPunchEvents(), policy);
        facts.put("workedMinutes", roundedWorkMinutes);

        rules.stream().filter(r -> !(r instanceof RoundingRules)).forEach(rule -> {
            if (rule.evaluate(context)) ruleResults.add(payPolicyRuleExecutor.executeRule(rule, context));
        });

        return context;
    }

    private WorkSession findWorkSessionForDate(List<PunchEvent> punchesInWindow, LocalDate workDate) {
        if (punchesInWindow == null || punchesInWindow.isEmpty()) return null;
        punchesInWindow.sort(Comparator.comparing(PunchEvent::getEventTime));
        for (int i = 0; i < punchesInWindow.size(); i++) {
            if (punchesInWindow.get(i).getPunchType() == PunchType.IN && punchesInWindow.get(i).getEventTime().toLocalDate().isEqual(workDate)) {
                int lastOutIndex = -1;
                for (int j = i + 1; j < punchesInWindow.size(); j++) {
                    if (punchesInWindow.get(j).getPunchType() == PunchType.OUT) {
                        lastOutIndex = j;
                    }
                }

                if (lastOutIndex != -1) {
                    return new WorkSession(punchesInWindow.subList(i, lastOutIndex + 1));
                } else {
                    return new WorkSession(punchesInWindow.subList(i, punchesInWindow.size()));
                }
            }
        }
        return null;
    }

    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
        if (punches == null || punches.isEmpty()) return 0;
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));

        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
            Optional<PunchEvent> firstIn = punches.stream()
                    .filter(p -> p.getPunchType() == PunchType.IN)
                    .findFirst();

            Optional<PunchEvent> lastOut = punches.stream()
                    .filter(p -> p.getPunchType() == PunchType.OUT)
                    .reduce((first, second) -> second);

            if (firstIn.isPresent() && lastOut.isPresent()) {
                return (int) Duration.between(firstIn.get().getEventTime(), lastOut.get().getEventTime()).toMinutes();
            } else {
                return 0;
            }
        } else {
            long totalMinutes = 0;
            LocalDateTime inTime = null;
            for (PunchEvent event : punches) {
                if (event.getPunchType() == PunchType.IN) {
                    if (inTime == null) inTime = event.getEventTime();
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

    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int totalWorkMinutes, int regularHoursMinutes, int excessHoursMinutes, List<PayPolicyRuleResultDTO> ruleResults, String status) {
        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
                .orElseGet(() -> Timesheet.builder().employeeId(employeeId).workDate(date).build());
        timesheet.setTotalWorkDurationMinutes(totalWorkMinutes);
        timesheet.setRegularHoursMinutes(regularHoursMinutes);
        timesheet.setExcessHoursMinutes(excessHoursMinutes);
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

    private class WorkSession {
        private final List<PunchEvent> punches;
        private final int totalMinutes;

        WorkSession(List<PunchEvent> punches) {
            this.punches = punches;
            this.totalMinutes = computeTotalWorkMinutes(punches, null);
        }

        public List<PunchEvent> getPunchEvents() { return punches; }
        public int getTotalMinutes() { return totalMinutes; }
    }
}