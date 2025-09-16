// Create a new file: harshwfm/wfm-backend/src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/LeavePolicyRuleResultDTO.java
package com.wfm.experts.setup.wfm.leavepolicy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeavePolicyRuleResultDTO {
    private String ruleName;
    private boolean result;
    private String message;
    private double balance;
}