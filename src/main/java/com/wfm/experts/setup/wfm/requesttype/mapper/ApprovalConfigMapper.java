package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.ApprovalConfigDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.ApprovalConfig;
import org.mapstruct.*;

@Mapper(config = MappersConfig.class)
public interface ApprovalConfigMapper {

    ApprovalConfigDTO toDto(ApprovalConfig entity);

    @Named("newApprovalConfig")
    @Mapping(target = "id", ignore = true)
    ApprovalConfig toNewEntity(ApprovalConfigDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget ApprovalConfig target, ApprovalConfigDTO source);
}
