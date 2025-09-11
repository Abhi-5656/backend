package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDto;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LeaveProfileMapper {

    @Mapping(target = "leavePolicyIds", expression = "java(mapPoliciesToIds(entity.getLeavePolicies()))")
    LeaveProfileDto toDto(LeaveProfile entity);

    @Mapping(target = "leavePolicies", ignore = true) // Handled in the service layer
    LeaveProfile toEntity(LeaveProfileDto dto);

    default Set<Long> mapPoliciesToIds(Set<LeavePolicy> policies) {
        if (policies == null) {
            return null;
        }
        return policies.stream()
                .map(LeavePolicy::getId)
                .collect(Collectors.toSet());
    }
}