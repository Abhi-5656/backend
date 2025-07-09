// LeavePolicyMapper.java
package com.wfm.experts.setup.wfm.leavepolicy.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring",
        uses = {
                ApplicableForMapper.class,
                AllowedFileTypesMapper.class,
                ConditionalRuleMapper.class
        }
)
public interface LeavePolicyMapper {

    LeavePolicyMapper INSTANCE = Mappers.getMapper(LeavePolicyMapper.class);

    @Mapping(source = "conditionalRules", target = "conditionalRules")
    LeavePolicyDTO toDto(LeavePolicy entity);

    @Mapping(target = "id", ignore = true) // if you don't want to overwrite PK on create
    @Mapping(source = "conditionalRules", target = "conditionalRules")
    LeavePolicy toEntity(LeavePolicyDTO dto);
}
