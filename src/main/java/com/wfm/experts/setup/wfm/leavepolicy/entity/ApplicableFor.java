// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/ApplicableFor.java
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines the shape for leave applicability settings, used when leave is measured in days.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableFor {
    private boolean fullDay;
    private boolean halfDay;
}
