// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/engine/executor/LeavePolicyRuleExecutor.java
package com.wfm.experts.setup.wfm.leavepolicy.engine.executor;

import com.wfm.experts.setup.wfm.leavepolicy.engine.context.LeavePolicyExecutionContext;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeaveBalanceRuleProvider;
import com.wfm.experts.setup.wfm.leavepolicy.rule.LeavePolicyRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LeavePolicyRuleExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LeavePolicyRuleExecutor.class);

    private final LeaveBalanceRuleProvider ruleProvider;

    public LeavePolicyRuleExecutor(LeaveBalanceRuleProvider ruleProvider) {
        this.ruleProvider = ruleProvider;
    }

    public double execute(LeavePolicyExecutionContext context) {
        double totalBalance = 0;
        for (LeavePolicyRule rule : ruleProvider.getRules()) {
            logger.info("Evaluating rule: {}", rule.getName());
            if (rule.evaluate(context)) {
                logger.info("Executing rule: {}", rule.getName());
                totalBalance += rule.execute(context).getBalance();
            } else {
                logger.info("Rule '{}' did not meet the criteria, skipping.", rule.getName());
            }
        }
        if (totalBalance == 0) {
            logger.warn("No leave policy rule was executed for employee {}. Defaulting to 0.", context.getEmployee().getEmployeeId());
        }
        return totalBalance;
    }
}