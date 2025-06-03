package com.wfm.experts.setup.wfm.paypolicy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPolicyRuleResultDTO {
    private String ruleName;
    private String result;      // e.g., "FULL_DAY", "OT_COMPUTED", etc.
    private boolean success;
    private String message;     // Optional info or error
}
