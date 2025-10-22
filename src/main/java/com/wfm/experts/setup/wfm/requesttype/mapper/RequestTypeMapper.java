package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.leavepolicy.mapper.LeavePolicyMapper;
import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        ApprovalConfigMapper.class,
        ClubbingConfigMapper.class,
        ValidationConfigMapper.class,
        NotificationConfigMapper.class,
        LeavePolicyMapper.class
})
public interface RequestTypeMapper {
    @Mapping(source = "leavePolicy", target = "leavePolicy")
    RequestType toEntity(RequestTypeDTO dto);

    @Mapping(source = "leavePolicy", target = "leavePolicy")
    RequestTypeDTO toDto(RequestType entity);
}