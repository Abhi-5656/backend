package com.wfm.experts.setup.wfm.paypolicy.engine.context;

import com.wfm.experts.modules.wfm.features.timesheet.entity.PunchEvent;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPolicyExecutionContext {
    private String employeeId; // <-- CHANGE THIS TO STRING
    private LocalDate date;
    private PayPolicy payPolicy;
    private List<PunchEvent> punchEvents;
    private Map<String, Object> facts; // e.g. "workedMinutes", "shiftId", etc.

    public Object getFact(String key) {
        return facts != null ? facts.get(key) : null;
    }

    public Integer getWorkedMinutes() {
        Object val = getFact("workedMinutes");
        if (val instanceof Integer) return (Integer) val;
        return null;
    }
    // Add similar helper getters for other common facts if needed.
}
