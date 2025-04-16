package com.wfm.experts.setup.orgstructure.service;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;

import java.util.List;

public interface JobTitleService {
    JobTitleDto create(JobTitleDto dto);
    JobTitleDto update(Long id, JobTitleDto dto);
    void delete(Long id);
    JobTitleDto getById(Long id);
    List<JobTitleDto> getAll();
}
