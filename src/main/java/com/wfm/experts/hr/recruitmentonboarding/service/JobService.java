package com.wfm.experts.hr.recruitmentonboarding.service;

import com.wfm.experts.hr.recruitmentonboarding.dto.JobDto;
import com.wfm.experts.hr.recruitmentonboarding.enums.ExperienceLevel;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobService {

    // 🔁 Basic CRUD
    JobDto create(JobDto jobDto);
    JobDto update(Long id, JobDto jobDto);
    Optional<JobDto> getById(Long id);
    List<JobDto> getAll();
    void delete(Long id);

    // 🔍 Custom Repository-backed Filters
    List<JobDto> getByStatus(JobStatus status);
    List<JobDto> getByExperienceLevel(ExperienceLevel experienceLevel);
    List<JobDto> searchByTitle(String keyword);
}
