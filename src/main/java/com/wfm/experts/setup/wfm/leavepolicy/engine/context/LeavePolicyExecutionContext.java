// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/setup/wfm/leavepolicy/engine/context/LeavePolicyExecutionContext.java
package com.wfm.experts.setup.wfm.leavepolicy.engine.context;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class LeavePolicyExecutionContext {
    private Employee employee;
    private LeavePolicy leavePolicy;
    private LocalDate leaveRequestDate;
    private Map<String, Object> facts;
}