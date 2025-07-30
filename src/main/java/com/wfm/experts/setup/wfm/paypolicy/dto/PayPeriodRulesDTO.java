package com.wfm.experts.setup.wfm.paypolicy.dto;

import com.wfm.experts.setup.wfm.paypolicy.enums.PayCalculationType;
import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayPeriodRulesDTO {
    private Long id;
    private boolean enabled;
    private PayCalculationType periodType;
    private String referenceDate;
    private String weekStart;
    private List<Integer> semiMonthlyDays;
//    private Map<String, Object> data; // Field for additional structured data

}
