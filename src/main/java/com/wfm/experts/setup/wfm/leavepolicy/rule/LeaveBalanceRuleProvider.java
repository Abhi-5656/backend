package com.wfm.experts.setup.wfm.leavepolicy.rule;

import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.DefaultLeaveBalanceRule;
import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.EarnedLeaveBalanceRule;
import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.ProrataLeaveBalanceRule;
import com.wfm.experts.setup.wfm.leavepolicy.rule.impl.RepeatedlyLeaveGrantRule;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LeaveBalanceRuleProvider {

    private final ProrataLeaveBalanceRule prorataLeaveBalanceRule;
    private final EarnedLeaveBalanceRule earnedLeaveBalanceRule;
    private final DefaultLeaveBalanceRule defaultLeaveBalanceRule;
    private final RepeatedlyLeaveGrantRule repeatedlyLeaveGrantRule;

    public LeaveBalanceRuleProvider(ProrataLeaveBalanceRule prorataLeaveBalanceRule,
                                    EarnedLeaveBalanceRule earnedLeaveBalanceRule,
                                    DefaultLeaveBalanceRule defaultLeaveBalanceRule,
                                    RepeatedlyLeaveGrantRule repeatedlyLeaveGrantRule) {
        this.prorataLeaveBalanceRule = prorataLeaveBalanceRule;
        this.earnedLeaveBalanceRule = earnedLeaveBalanceRule;
        this.defaultLeaveBalanceRule = defaultLeaveBalanceRule;
        this.repeatedlyLeaveGrantRule = repeatedlyLeaveGrantRule;
    }

    public List<LeavePolicyRule> getRules() {
        return Arrays.asList(
                prorataLeaveBalanceRule,
                earnedLeaveBalanceRule,
                repeatedlyLeaveGrantRule,
                defaultLeaveBalanceRule
        );
    }
}