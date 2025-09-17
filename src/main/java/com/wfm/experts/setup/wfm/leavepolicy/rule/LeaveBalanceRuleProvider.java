package com.wfm.experts.setup.wfm.leavepolicy.rule;

import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.DefaultLeaveBalanceRule;
import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.EarnedLeaveBalanceRule;
import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.ProrataLeaveBalanceRule;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LeaveBalanceRuleProvider {

    private final EarnedLeaveBalanceRule earnedLeaveBalanceRule;

    public LeaveBalanceRuleProvider(EarnedLeaveBalanceRule earnedLeaveBalanceRule) {
        this.earnedLeaveBalanceRule = earnedLeaveBalanceRule;
    }

    /**
     * Defines the chain of responsibility for leave balance calculation rules.
     * Rules are evaluated in the order they are added to this list.
     * The first rule to have its `evaluate` method return true will be executed.
     *
     * @return An ordered list of leave policy rules.
     */
    public List<LeavePolicyRule> getRules() {
        return Arrays.asList(
                // Most specific rules should come first.
                new ProrataLeaveBalanceRule(),
                earnedLeaveBalanceRule,
                // The default rule should always be last as a fallback.
                new DefaultLeaveBalanceRule()
        );
    }
}