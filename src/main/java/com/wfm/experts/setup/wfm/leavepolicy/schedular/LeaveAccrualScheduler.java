//package com.wfm.experts.setup.wfm.leavepolicy.schedular;
//
//import com.wfm.experts.repository.core.SubscriptionRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
//import com.wfm.experts.tenancy.TenantContext;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.YearMonth;
//import java.time.LocalDate;
//
//@Component
//@RequiredArgsConstructor
//public class LeaveAccrualScheduler {
//
//    private final LeaveAccrualService leaveAccrualService;
//    private final SubscriptionRepository subscriptionRepository;
//
//    // This cron expression is for testing and will run at 1:16 PM on September 23rd
//    @Scheduled(cron = "0 16 13 23 9 ?", zone = "Asia/Kolkata")
//    public void runMonthlyLeaveAccrual() {
//        subscriptionRepository.findAll().forEach(subscription -> {
//            try {
//                TenantContext.setTenant(subscription.getTenantId());
//                leaveAccrualService.accrueLeaveForMonth(YearMonth.now());
//            } finally {
//                TenantContext.clear();
//            }
//        });
//    }
//}
//the above code has been written for testing purpose to force the schedular to run anytime

package com.wfm.experts.setup.wfm.leavepolicy.schedular;

import com.wfm.experts.repository.core.SubscriptionRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LeaveAccrualScheduler {

    private final LeaveAccrualService leaveAccrualService;
    private final SubscriptionRepository subscriptionRepository;

    // This cron expression runs at 1 AM on the last day of every month
    @Scheduled(cron = "0 0 1 L * ?", zone = "Asia/Kolkata")
    public void runMonthlyLeaveAccrual() {
        LocalDate today = LocalDate.now();
        // Check if today is the last day of the month
        if (today.getDayOfMonth() == today.lengthOfMonth()) {
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
}