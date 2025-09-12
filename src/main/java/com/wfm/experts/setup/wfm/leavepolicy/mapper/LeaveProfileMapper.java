package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicySettingDto; // <-- ADD THIS IMPORT
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfilePolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveProfileMapper {

    @Mapping(target = "leavePolicySettings", source = "leaveProfilePolicies")
    LeaveProfileDto toDto(LeaveProfile entity);

    // This method is implicitly used by the mapping above
    @Mapping(source = "leavePolicy.id", target = "policyId")
    // Use the imported class directly
    LeavePolicySettingDto profilePolicyToSettingDto(LeaveProfilePolicy profilePolicy);
}