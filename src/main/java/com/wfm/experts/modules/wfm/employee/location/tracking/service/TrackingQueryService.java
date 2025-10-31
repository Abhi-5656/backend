package com.wfm.experts.modules.wfm.employee.location.tracking.service;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;

public interface TrackingQueryService {
    LiveResponse getLive(String employeeId);
    // add getHistory(...) if needed now
}
