package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.NotificationConfigDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.NotificationConfig;
import org.mapstruct.*;

@Mapper(config = MappersConfig.class)
public interface NotificationConfigMapper {

    // Entity -> DTO
    NotificationConfigDTO toDto(NotificationConfig entity);

    // DTO -> Entity (CREATE)
    @Named("newNotificationConfig")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "channels", source = "channels")
    @Mapping(target = "empSubmit", source = "empSubmit")
    @Mapping(target = "empApprove", source = "empApprove")
    @Mapping(target = "empReject", source = "empReject")
    @Mapping(target = "empCancel", source = "empCancel")
    @Mapping(target = "approverSubmit", source = "approverSubmit")
    @Mapping(target = "approverEscalate", source = "approverEscalate")
    @Mapping(target = "approverCancel", source = "approverCancel")
    @Mapping(target = "approverDelete", source = "approverDelete")
    @Mapping(target = "listenerSubmit", source = "listenerSubmit")
    @Mapping(target = "listenerApprove", source = "listenerApprove")
    @Mapping(target = "listenerReject", source = "listenerReject")
    @Mapping(target = "listenerCancel", source = "listenerCancel")
    @Mapping(target = "listenerDelete", source = "listenerDelete")
    NotificationConfig toNewEntity(NotificationConfigDTO dto);

    // DTO -> Entity (UPDATE / PATCH)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget NotificationConfig target, NotificationConfigDTO source);
}
