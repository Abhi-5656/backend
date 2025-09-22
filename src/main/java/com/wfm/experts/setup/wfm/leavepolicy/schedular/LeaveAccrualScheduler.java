//package com.wfm.experts.setup.wfm.leavepolicy.schedular;
//
//import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.YearMonth;
//
//@Component
//@RequiredArgsConstructor
//public class LeaveAccrualScheduler {
//
//    private final LeaveAccrualService leaveAccrualService;
//
//    @Scheduled(cron = "0 33 18 * * ?", zone = "Asia/Kolkata") // Runs at 6:26 PM IST
//    public void runMonthlyLeaveAccrual() {
//        leaveAccrualService.accrueLeaveForMonth(YearMonth.now());
//    }
//}
package com.wfm.experts.setup.wfm.leavepolicy.schedular;

import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class LeaveAccrualScheduler {

    private final LeaveAccrualService leaveAccrualService;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "* * * * * *", zone = "Asia/Kolkata") // Runs every second
    public void runMonthlyLeaveAccrual() {
        subscriptionRepository.findAll().forEach(subscription -> {
            try {
                TenantContext.setTenant(subscription.getTenantId());
                leaveAccrualService.accrueLeaveForMonth(YearMonth.now());
            } finally {
                TenantContext.clear();
            }
        });
    }
}