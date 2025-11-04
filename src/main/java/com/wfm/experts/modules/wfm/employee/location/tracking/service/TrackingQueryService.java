package com.wfm.experts.modules.wfm.employee.location.tracking.service;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.LiveResponse;

public interface TrackingQueryService {
    /**
     * Live view for one session owned by the employee.
     * Throws if the session doesn't exist or doesn't belong to the employee.
     */
    LiveResponse live(String employeeId, Long sessionId);
}
