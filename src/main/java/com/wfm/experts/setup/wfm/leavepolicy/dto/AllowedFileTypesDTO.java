package com.wfm.experts.setup.wfm.leavepolicy.dto;// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/dto/AllowedFileTypesDTO.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for allowed attachment file types.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllowedFileTypesDTO {
    private boolean pdf;
    private boolean jpg;
    private boolean png;
    private boolean docx;
}
