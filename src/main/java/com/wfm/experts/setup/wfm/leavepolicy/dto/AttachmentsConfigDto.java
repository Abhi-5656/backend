package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.AllowedFileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentsConfigDto {
    private boolean enabled;
    private List<AllowedFileType> allowedFileTypes;
}