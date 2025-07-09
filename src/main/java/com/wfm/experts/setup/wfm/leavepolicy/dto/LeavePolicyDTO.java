// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/LeavePolicyDTO.java
package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.wfm.experts.setup.wfm.leavepolicy.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing the entire leave policy configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicyDTO {
    private String leaveName;
    private String code;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;

    private ProfileSelect profileSelect;
    private String calendarColor;
    private boolean enableLeaveConfig;

    private MeasureBy measureBy;
    private PaidUnpaid paidUnpaid;

    private ApplicableForDTO applicableFor;

    private Integer maxDaysYear;
    private Integer maxDaysMonth;
    private Integer maxConsecutiveDays;

    private Integer minAdvanceNotice;
    private Integer minWorked;

    private Integer occurrenceLimit;
    private OccurrencePeriod occurrencePeriod;

    private boolean enableCarryOverProration;
    private boolean allowCarryOver;
    private Integer carryOverCap;

    private CalculationBasis calculationBasis;
    private boolean autoEncash;
    private boolean allowProration;

    private ProrationMode prorationMode;
    private Integer joinDateThreshold;

    private RoundingMode rounding;

    private boolean enableAttachments;
    private boolean attachmentRequired;

    private AllowedFileTypesDTO allowedFileTypes;

    private List<ConditionalRuleDTO> conditionalRules;
}
