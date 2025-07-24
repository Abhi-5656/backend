// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/mapper/LeaveProfileMapper.java
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.dto.LeaveProfileDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LeaveProfileMapper {

    /**
     * Map incoming DTO → entity.
     * leavePolicies must be set in your service (lookup by IDs), so ignore here.
     */
    @Mapping(target = "leavePolicies", ignore = true)
    LeaveProfile toEntity(LeaveProfileDTO dto);

    /**
     * Map entity → DTO.
     * Convert Set<LeavePolicy> → List<Long> leaveIds via helper.
     */
    @Mapping(target = "leaveIds", source = "leavePolicies")
    LeaveProfileDTO toDto(LeaveProfile profile);

    /** Helper to map policies to their IDs */
    default List<Long> map(Set<LeavePolicy> leavePolicies) {
        if (leavePolicies == null) return null;
        return leavePolicies.stream()
                .map(LeavePolicy::getId)
                .collect(Collectors.toList());
    }
}
