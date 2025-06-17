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
    private final EmployeeShiftRepository employeeShiftRepository; // Added for shift context
    private final PayPolicyRuleExecutor payPolicyRuleExecutor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processPunchEvents(String employeeId, LocalDate date) {
        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);

        PayPolicyAssignment assignment = payPolicyAssignmentRepository
                .findActiveAssignment(employeeId, date)
                .orElse(null);

        if (assignment == null) {
            log.warn("No active PayPolicyAssignment found for employee: {} on {}", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList(), null, null);
            return;
        }

        PayPolicy policy = payPolicyRepository.findById(assignment.getPayPolicyId())
                .orElse(null);
        if (policy == null) {
            log.error("PayPolicy {} not found for assignment {}", assignment.getPayPolicyId(), assignment.getId());
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList(), null, null);
            return;
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        List<PunchEvent> punches = punchEventRepository
                .findAllByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);

        if (punches.isEmpty()) {
            log.info("No punch events for employee: {} on date: {}. Clearing timesheet.", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList(), null, null);
            return;
        }

        // --- Enrich Context ---
        int totalWorkMinutes = computeTotalWorkMinutes(punches);
        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);
        // In a real scenario, you would also check against a holiday calendar.
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
                .build();

        List<PayPolicyRule> rules = policy.getRules();
        List<PayPolicyRuleResultDTO> ruleResults = payPolicyRuleExecutor.executeRules(rules, context);

        // --- Extract final results from context after rule execution ---
        Integer finalWorkMinutes = (Integer) context.getFacts().getOrDefault("workedMinutes", totalWorkMinutes);
        Integer overtimeMinutes = (Integer) context.getFacts().get("overtimeMinutes");

        saveOrUpdateTimesheet(employeeId, date, finalWorkMinutes, ruleResults, overtimeMinutes, "PENDING");

        log.info("Timesheet processed for employee: {} on date: {} ({} minutes, {} rules)",
                employeeId, date, finalWorkMinutes, ruleResults.size());
    }

    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int workMinutes, List<PayPolicyRuleResultDTO> ruleResults, Integer overtime, String status) {
        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
                .orElseGet(() -> Timesheet.builder()
                        .employeeId(employeeId)
                        .workDate(date)
                        .build());

        timesheet.setWorkDurationMinutes(workMinutes);
        timesheet.setTotalWorkDuration(workMinutes / 60.0);
        timesheet.setOvertimeDuration(overtime);
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

    private int computeTotalWorkMinutes(List<PunchEvent> punches) {
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
        long totalMinutes = 0;
        LocalDateTime inTime = null;

        for (PunchEvent event : punches) {
            if (event.getPunchType() == PunchType.IN) {
                if (inTime == null) { // Start of a new work segment
                    inTime = event.getEventTime();
                }
            } else if (event.getPunchType() == PunchType.OUT) {
                if (inTime != null) { // End of a work segment
                    totalMinutes += Duration.between(inTime, event.getEventTime()).toMinutes();
                    inTime = null; // Reset for the next segment
                }
            }
        }
        return (int) Math.max(0, totalMinutes);
    }
}