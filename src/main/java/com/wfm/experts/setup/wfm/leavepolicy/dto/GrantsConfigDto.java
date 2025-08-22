package com.wfm.experts.setup.wfm.leavepolicy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantExpiration;
import com.wfm.experts.setup.wfm.leavepolicy.enums.GrantType;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantsConfigDto {
    private boolean enabled;
    private GrantType grantType;
    private GrantExpiration expiration;

    @Valid
    private FixedGrantConfigDto fixedGrant;

    @Valid
    private EarnedGrantConfigDto earnedGrant;
}