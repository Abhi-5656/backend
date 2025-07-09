// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/ApplicableForDTO.java
package com.wfm.experts.setup.wfm.leavepolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for leave applicability settings, when leave is measured in days.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableForDTO {
    private boolean fullDay;
    private boolean halfDay;
}
