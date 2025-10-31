package com.wfm.experts.modules.wfm.employee.location.tracking.service;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;

public interface TrackingChunkService {
    void ingestPoint(TrackingPointMessage msg);
    void closeSession(TrackingCloseMessage msg);
}
