package com.wfm.experts.tenant.common.subscription.mapper;

import com.wfm.experts.tenant.common.subscription.dto.SubscriptionDTO;
import com.wfm.experts.tenant.common.subscription.dto.SubscriptionModuleDTO;
import com.wfm.experts.tenant.common.subscription.entity.Subscription;
import com.wfm.experts.tenant.common.subscription.entity.SubscriptionModule;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionMapper INSTANCE = Mappers.getMapper(SubscriptionMapper.class);

    // Entity -> DTO
    SubscriptionDTO toDto(Subscription entity);
    SubscriptionModuleDTO toDto(SubscriptionModule entity);
    List<SubscriptionDTO> toDtoList(List<Subscription> entities);

    // DTO -> Entity (kept for future, not required to change existing controller)
    Subscription toEntity(SubscriptionDTO dto);
    SubscriptionModule toEntity(SubscriptionModuleDTO dto);
    List<Subscription> toEntityList(List<SubscriptionDTO> dtos);
}
