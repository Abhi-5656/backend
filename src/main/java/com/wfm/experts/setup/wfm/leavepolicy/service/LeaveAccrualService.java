package com.wfm.experts.setup.wfm.leavepolicy.service;

import java.time.YearMonth;

public interface LeaveAccrualService {
    void accrueLeaveForMonth(YearMonth month);
}