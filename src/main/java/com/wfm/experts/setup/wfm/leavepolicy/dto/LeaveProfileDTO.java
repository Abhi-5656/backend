package com.wfm.experts.setup.wfm.leavepolicy.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO for creating or updating a LeaveProfile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveProfileDTO {

    /** e.g. "Standard Employee Profile" **/
    @NotBlank(message = "profileName is required")
    private String profileName;

    /**
     * List of LeavePolicy IDs to associate with this profile.
     * e.g. [1, 5, 12]
     */
    @NotEmpty(message = "leaveIds must contain at least one LeavePolicy ID")
    private List<Long> leaveIds;
}
