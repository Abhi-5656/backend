package com.wfm.experts.hr.recruitmentonboarding.application.service;


import com.wfm.experts.hr.recruitmentonboarding.application.dto.JobApplicationDto;

import java.util.List;

public interface JobApplicationService {

    JobApplicationDto applyForJob(JobApplicationDto dto);

    List<JobApplicationDto> getApplicationsByJobId(Long jobId);

    JobApplicationDto getApplicationById(Long id);
}
