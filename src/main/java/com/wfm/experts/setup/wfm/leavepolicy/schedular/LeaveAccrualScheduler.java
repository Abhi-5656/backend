package com.wfm.experts.setup.wfm.leavepolicy.schedular;

import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class LeaveAccrualScheduler {

    private final LeaveAccrualService leaveAccrualService;

    @Scheduled(cron = "0 33 18 * * ?", zone = "Asia/Kolkata") // Runs at 6:26 PM IST
    public void runMonthlyLeaveAccrual() {
        leaveAccrualService.accrueLeaveForMonth(YearMonth.now());
    }
}