// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/setup/wfm/leavepolicy/rule/LeavePolicyRule.java
package com.wfm.experts.setup.wfm.leavepolicy.rule;

import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyRuleResultDTO;

public interface LeavePolicyRule {
    String getName();
    boolean evaluate(LeavePolicyExecutionContext context);
    LeavePolicyRuleResultDTO execute(LeavePolicyExecutionContext context);
}