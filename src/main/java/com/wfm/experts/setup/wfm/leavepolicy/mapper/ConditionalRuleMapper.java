// src/main/java/com/wfm/experts/setup/wfm/leavepolicy/mapper/ConditionalRuleMapper.java
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ConditionalRuleDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ConditionalRule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConditionalRuleMapper {

    // Map entity → DTO (no need to mention leavePolicy, DTO doesn’t have it)
    ConditionalRuleDTO toDto(ConditionalRule entity);

    // Map DTO → entity; leavePolicy will be set in your service, so ignore id if you like
    ConditionalRule toEntity(ConditionalRuleDTO dto);
}
