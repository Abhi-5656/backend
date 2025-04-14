package com.wfm.experts.hr.recruitmentonboarding.application.mapper;

import com.wfm.experts.hr.recruitmentonboarding.application.dto.CertificationDto;
import com.wfm.experts.hr.recruitmentonboarding.application.entity.Certification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CertificationMapper {
    CertificationDto toDto(Certification cert);
    Certification toEntity(CertificationDto dto);
}
