package com.wfm.experts.setup.wfm.requesttype.mapper;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.entity.ApprovalConfig;
import com.wfm.experts.setup.wfm.requesttype.entity.ClubbingConfig;
import com.wfm.experts.setup.wfm.requesttype.entity.ValidationConfig;
import com.wfm.experts.setup.wfm.requesttype.entity.NotificationConfig;
import org.mapstruct.*;

@Mapper(
        config = MappersConfig.class,
        uses = {
                ApprovalConfigMapper.class,
                ClubbingConfigMapper.class,
                ValidationConfigMapper.class,
                NotificationConfigMapper.class
        }
)
public interface RequestTypeMapper {

    // -------- Entity -> DTO --------
    RequestTypeDTO toDto(RequestType entity);

    // -------- DTO -> Entity (CREATE) --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "approval",    source = "approval",    qualifiedByName = "newApprovalConfig")
    @Mapping(target = "clubbing",    source = "clubbing",    qualifiedByName = "newClubbingConfig")
    @Mapping(target = "validation",  source = "validation",  qualifiedByName = "newValidationConfig")
    @Mapping(target = "notifications", source = "notifications", qualifiedByName = "newNotificationConfig")
    RequestType toNewEntity(RequestTypeDTO dto);

    // -------- DTO -> Entity (UPDATE/PATCH) --------
    // Let MapStruct map scalar fields; we’ll manually delegate nested updates in @AfterMapping
    void updateEntity(@MappingTarget RequestType target, RequestTypeDTO source);

    // Delegate nested updates (PATCH semantics honored by child mappers via IGNORE)
    @AfterMapping
    default void afterUpdateEntity(@MappingTarget RequestType target,
                                   RequestTypeDTO source,
                                   ApprovalConfigMapper approvalMapper,
                                   ClubbingConfigMapper clubbingMapper,
                                   ValidationConfigMapper validationMapper,
                                   NotificationConfigMapper notificationMapper) {

        if (source.getApproval() != null) {
            ApprovalConfig child = target.getApproval();
            if (child == null) child = approvalMapper.toNewEntity(source.getApproval());
            else approvalMapper.updateEntity(child, source.getApproval());
            target.setApproval(child);
        }
        if (source.getClubbing() != null) {
            ClubbingConfig child = target.getClubbing();
            if (child == null) child = clubbingMapper.toNewEntity(source.getClubbing());
            else clubbingMapper.updateEntity(child, source.getClubbing());
            target.setClubbing(child);
        }
        if (source.getValidation() != null) {
            ValidationConfig child = target.getValidation();
            if (child == null) child = validationMapper.toNewEntity(source.getValidation());
            else validationMapper.updateEntity(child, source.getValidation());
            target.setValidation(child);
        }
        if (source.getNotifications() != null) {
            NotificationConfig child = target.getNotifications();
            if (child == null) child = notificationMapper.toNewEntity(source.getNotifications());
            else notificationMapper.updateEntity(child, source.getNotifications());
            target.setNotifications(child);
        }
    }
}
