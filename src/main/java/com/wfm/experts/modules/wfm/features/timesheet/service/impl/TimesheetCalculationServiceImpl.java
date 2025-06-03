package com.wfm.experts.modules.wfm.features.timesheet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.entity.PayPolicyAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.paypolicy.repository.PayPolicyAssignmentRepository;
import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
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
    private final PayPolicyRuleExecutor payPolicyRuleExecutor;
    private final ObjectMapper objectMapper; // For serializing rule results to JSON

    @Override
    @Transactional
    public void processPunchEvents(String employeeId, LocalDate date) {
        log.info("Processing timesheet for employee: {} on date: {}", employeeId, date);

        // 1. Find the active pay policy assignment for this employee on this date
        Optional<PayPolicyAssignment> assignmentOpt =
                payPolicyAssignmentRepository.findByEmployeeIdAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqual(
                        employeeId, date, date);

        if (assignmentOpt.isEmpty()) {
            log.warn("No active PayPolicyAssignment found for employee: {} on {}", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList());
            return;
        }
        PayPolicyAssignment assignment = assignmentOpt.get();

        // 2. Fetch PayPolicy
        Optional<PayPolicy> policyOpt = payPolicyRepository.findById(assignment.getPayPolicyId());
        if (policyOpt.isEmpty()) {
            log.error("PayPolicy {} not found for assignment {}", assignment.getPayPolicyId(), assignment.getId());
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList());
            return;
        }
        PayPolicy policy = policyOpt.get();

        // 3. Get all punches for employee on this date (midnight to 23:59:59)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<PunchEvent> punches = punchEventRepository
                .findByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);

        if (punches.isEmpty()) {
            log.warn("No punch events for employee: {} on date: {}", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, Collections.emptyList());
            return;
        }

        // 4. Build the execution context for rules
        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
                .employeeId(employeeId)
                .date(date)
                .payPolicy(policy)
                .punchEvents(punches)
                .build();

        // 5. Extract and execute rules
        List<PayPolicyRule> rules = extractRulesFromPolicy(policy);
        List<PayPolicyRuleResultDTO> ruleResults = payPolicyRuleExecutor.executeRules(rules, context);

        // 6. Compute total work minutes (custom logic, e.g., sum duration between IN/OUT pairs)
        int totalWorkMinutes = computeTotalWorkMinutes(punches);

        // 7. Save/update Timesheet entity
        saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, ruleResults);

        log.info("Timesheet processed for employee: {} on date: {} ({} minutes, {} rules)", employeeId, date, totalWorkMinutes, ruleResults.size());
    }

    /**
     * INTERNAL HELPER: Assembles rule objects configured on PayPolicy into a list for rule execution.
     */
    private List<PayPolicyRule> extractRulesFromPolicy(PayPolicy policy) {
        List<PayPolicyRule> rules = new ArrayList<>();
        if (policy.getAttendanceRule() != null) rules.add(policy.getAttendanceRule());
        if (policy.getRoundingRules() != null) rules.add(policy.getRoundingRules());
        if (policy.getPunchEventRules() != null) rules.add(policy.getPunchEventRules());
        if (policy.getBreakRules() != null) rules.add(policy.getBreakRules());
        if (policy.getOvertimeRules() != null) rules.add(policy.getOvertimeRules());
        if (policy.getPayPeriodRules() != null) rules.add(policy.getPayPeriodRules());
        if (policy.getHolidayPayRules() != null) rules.add(policy.getHolidayPayRules());
        return rules;
    }

    /**
     * Helper to create/update timesheet with rule results stored as JSON.
     */
    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int workMinutes, List<PayPolicyRuleResultDTO> ruleResults) {
        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(employeeId, date)
                .orElseGet(() -> Timesheet.builder()
                        .employeeId(employeeId)
                        .workDate(date) // <-- CORRECT FIELD NAME!
                        .build());
        timesheet.setWorkDurationMinutes(workMinutes);
        // ... other setters


    // Store rule results as JSON (for traceability)
        try {
            timesheet.setRuleResultsJson(objectMapper.writeValueAsString(ruleResults));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rule results for employee: {} on date: {}", employeeId, date, e);
            timesheet.setRuleResultsJson("[]");
        }
        timesheet.setCalculatedAt(LocalDate.now());

        timesheetRepository.save(timesheet);
    }

    /**
     * Example work-minutes calculation from punch events (replace with your logic).
     * This version: Sums durations between all consecutive IN and OUT punches.
     */
    private int computeTotalWorkMinutes(List<PunchEvent> punches) {
        punches.sort(Comparator.comparing(PunchEvent::getEventTime));
        int totalMinutes = 0;
        LocalDateTime inTime = null;
        for (PunchEvent event : punches) {
            if (event.getPunchType() != null) {
                switch (event.getPunchType()) {
                    case IN:
                        inTime = event.getEventTime();
                        break;
                    case OUT:
                        if (inTime != null) {
                            totalMinutes += (int) java.time.Duration.between(inTime, event.getEventTime()).toMinutes();
                            inTime = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return Math.max(0, totalMinutes);
    }
}
