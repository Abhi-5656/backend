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
        for (LeavePolicyRule rule : ruleProvider.getRules()) {
            logger.info("Evaluating rule: {}", rule.getName());
            if (rule.evaluate(context)) {
                logger.info("Executing rule: {}", rule.getName());
                return rule.execute(context).getBalance();
            }
            logger.info("Rule '{}' did not meet the criteria, skipping.", rule.getName());
        }
        logger.warn("No leave policy rule was executed for employee {}. Defaulting to 0.", context.getEmployee().getEmployeeId());
        return 0;
    }
}