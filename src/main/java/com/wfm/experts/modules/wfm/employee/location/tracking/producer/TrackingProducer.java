package com.wfm.experts.modules.wfm.employee.location.tracking.producer;

import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingCloseMessage;
import com.wfm.experts.modules.wfm.employee.location.tracking.dto.TrackingPointMessage;

public interface TrackingProducer {
    void publishPoint(TrackingPointMessage msg);
    void publishClose(TrackingCloseMessage msg);
}
