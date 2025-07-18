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

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        List<PunchEvent> punches = punchEventRepository
                .findAllByEmployeeIdAndEventTimeBetween(employeeId, startOfDay, endOfDay);

        if (punches.isEmpty()) {
            log.info("No punch events for employee: {} on date: {}. Clearing timesheet.", employeeId, date);
            saveOrUpdateTimesheet(employeeId, date, 0, 0, 0, 0, 0, Collections.emptyList(), "Absent");
            return;
        }

        PayPolicyAssignment assignment = payPolicyAssignmentRepository
                .findActiveAssignment(employeeId, date)
                .orElse(null);

        int totalWorkMinutes = computeTotalWorkMinutes(punches, null);
        EmployeeShift currentShift = employeeShiftRepository.findByEmployeeIdAndCalendarDate(employeeId, date).orElse(null);

        if (assignment == null) {
            log.warn("No active PayPolicyAssignment for employee: {} on {}. Calculating without rules.", employeeId, date);
            int regularMinutes = totalWorkMinutes;
            int excessMinutes = 0;
            if (currentShift != null && currentShift.getShift() != null) {
                long shiftDurationInMinutes = Duration.between(currentShift.getShift().getStartTime(), currentShift.getShift().getEndTime()).toMinutes();
                if (shiftDurationInMinutes < 0) shiftDurationInMinutes += 1440;
                regularMinutes = (int) Math.min(totalWorkMinutes, shiftDurationInMinutes);
                excessMinutes = totalWorkMinutes - regularMinutes;
            }
            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, regularMinutes, 0, excessMinutes, 0, Collections.emptyList(), "Present");
            return;
        }

        PayPolicy policy = payPolicyRepository.findById(assignment.getPayPolicyId()).orElse(null);

        if (policy == null) {
            log.error("PayPolicy {} not found for assignment {}. Calculating without rules.", assignment.getPayPolicyId(), assignment.getId());
            int regularMinutes = totalWorkMinutes;
            int excessMinutes = 0;
            if (currentShift != null && currentShift.getShift() != null) {
                long shiftDurationInMinutes = Duration.between(currentShift.getShift().getStartTime(), currentShift.getShift().getEndTime()).toMinutes();
                if (shiftDurationInMinutes < 0) shiftDurationInMinutes += 1440;
                regularMinutes = (int) Math.min(totalWorkMinutes, shiftDurationInMinutes);
                excessMinutes = totalWorkMinutes - regularMinutes;
            }
            saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, regularMinutes, 0, excessMinutes, 0, Collections.emptyList(), "Present");
            return;
        }

        Map<String, Object> facts = new HashMap<>();
        facts.put("workedMinutes", totalWorkMinutes);
        facts.put("shift", currentShift);
        facts.put("isHoliday", false);

        PayPolicyExecutionContext context = PayPolicyExecutionContext.builder()
                .employeeId(employeeId).date(date).payPolicy(policy)
                .punchEvents(new ArrayList<>(punches)).facts(facts)
                .timesheetRepository(timesheetRepository).build();

        List<PayPolicyRule> rules = policy.getRules();
        List<PayPolicyRuleResultDTO> ruleResults = payPolicyRuleExecutor.executeRules(rules, context);

        Integer netPayableMinutes = (Integer) context.getFacts().getOrDefault("workedMinutes", totalWorkMinutes);
        Integer dailyOtMinutes = (Integer) context.getFacts().getOrDefault("dailyOtHoursMinutes", 0);
        Integer weeklyOtMinutes = (Integer) context.getFacts().getOrDefault("weeklyOtHoursMinutes", 0);
        Integer excessMinutes = (Integer) context.getFacts().getOrDefault("excessHoursMinutes", 0);

        int totalOvertimeMinutes = dailyOtMinutes + weeklyOtMinutes + excessMinutes;
        int regularMinutes = netPayableMinutes - totalOvertimeMinutes;

        regularMinutes = Math.max(0, regularMinutes);

        String finalStatus = ruleResults.stream()
                .filter(r -> "AttendanceRule".equals(r.getRuleName()))
                .map(PayPolicyRuleResultDTO::getResult)
                .findFirst().orElse("Present");

        saveOrUpdateTimesheet(employeeId, date, totalWorkMinutes, regularMinutes, dailyOtMinutes, excessMinutes, weeklyOtMinutes, ruleResults, finalStatus);
    }

    private void saveOrUpdateTimesheet(String employeeId, LocalDate date, int totalWorkMinutes, int regularHoursMinutes, int dailyOtHoursMinutes, int excessHoursMinutes, int weeklyOtHoursMinutes, List<PayPolicyRuleResultDTO> ruleResults, String status) {
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

    private int computeTotalWorkMinutes(List<PunchEvent> punches, PayPolicy policy) {
        if (punches == null || punches.isEmpty()) return 0;

        if (policy != null && Boolean.TRUE.equals(policy.getUseFiloCalculation())) {
            log.debug("Using FILO calculation logic.");
            Optional<LocalDateTime> firstIn = punches.stream().filter(p -> p.getPunchType() == PunchType.IN).map(PunchEvent::getEventTime).min(LocalDateTime::compareTo);
            Optional<LocalDateTime> lastOut = punches.stream().filter(p -> p.getPunchType() == PunchType.OUT).map(PunchEvent::getEventTime).max(LocalDateTime::compareTo);
            if (firstIn.isPresent() && lastOut.isPresent() && lastOut.get().isAfter(firstIn.get())) {
                return (int) Duration.between(firstIn.get(), lastOut.get()).toMinutes();
            }
            return 0;
        } else {
            log.debug("Using standard paired punch calculation logic.");
            punches.sort(Comparator.comparing(PunchEvent::getEventTime));
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
}