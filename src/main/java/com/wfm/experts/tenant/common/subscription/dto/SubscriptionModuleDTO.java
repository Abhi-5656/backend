package com.wfm.experts.tenant.common.subscription.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class SubscriptionModuleDTO {
    private Long id;

    @NotBlank(message = "moduleName is required")
    private String moduleName; // e.g., HR, WFM, Payroll
}
