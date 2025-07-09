// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/entity/AllowedFileTypes.java
package com.wfm.experts.setup.wfm.leavepolicy.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines the shape for allowed attachment file types for a leave policy.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedFileTypes {
    private boolean pdf;
    private boolean jpg;
    private boolean png;
    private boolean docx;
}
