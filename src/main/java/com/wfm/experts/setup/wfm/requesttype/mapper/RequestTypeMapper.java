// src/main/java/com/wfm/experts/setup/wfm/requesttype/mapper/RequestTypeMapper.java
package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.mapper.LeavePolicyMapper;
import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        ApprovalConfigMapper.class,
        ClubbingConfigMapper.class,
        ValidationConfigMapper.class,
        NotificationConfigMapper.class,
        LeavePolicyMapper.class // Added LeavePolicyMapper
})
public interface RequestTypeMapper {
    RequestType toEntity(RequestTypeDTO dto);
    RequestTypeDTO toDto(RequestType entity);
}