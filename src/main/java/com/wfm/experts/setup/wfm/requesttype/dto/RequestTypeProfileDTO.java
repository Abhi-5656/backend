package com.wfm.experts.setup.wfm.requesttype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class RequestTypeProfileDTO {

    private Long id;

    @NotBlank(message = "Profile name cannot be blank.")
    private String profileName;

    @NotEmpty(message = "At least one request type must be selected.")
    private List<Long> requestTypeIds;
}