package com.wfm.experts.setup.orgstructure.mapper;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobTitleMapper {



    JobTitleDto toDto(JobTitle jobTitle);

    List<JobTitleDto> toDtoList(List<JobTitle> jobTitles);

    JobTitle toEntity(JobTitleDto dto);
}
