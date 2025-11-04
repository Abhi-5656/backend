// com/wfm/experts/modules/wfm/employee/location/tracking/service/TrackingCommandService.java
package com.wfm.experts.modules.wfm.employee.location.tracking.service;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockInRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.ClockOutRequest;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.PointRequest;

public interface TrackingCommandService {
    Long startSession(String employeeId, ClockInRequest req);
    void addPoint(String employeeId, PointRequest req);
    void closeSession(String employeeId, ClockOutRequest req);
}
