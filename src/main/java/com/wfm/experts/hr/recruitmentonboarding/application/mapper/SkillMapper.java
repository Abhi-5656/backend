package com.wfm.experts.hr.recruitmentonboarding.application.mapper;

import com.wfm.experts.hr.recruitmentonboarding.application.dto.SkillDto;
import com.wfm.experts.hr.recruitmentonboarding.application.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDto toDto(Skill skill);
    Skill toEntity(SkillDto dto);
}

