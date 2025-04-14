package com.wfm.experts.hr.recruitmentonboarding.application.service.impl;

import com.wfm.experts.hr.recruitmentonboarding.application.dto.JobApplicationDto;
import com.wfm.experts.hr.recruitmentonboarding.application.entity.JobApplication;
import com.wfm.experts.hr.recruitmentonboarding.application.exception.ResourceNotFoundException;
import com.wfm.experts.hr.recruitmentonboarding.application.mapper.JobApplicationMapper;
import com.wfm.experts.hr.recruitmentonboarding.application.repository.JobApplicationRepository;
import com.wfm.experts.hr.recruitmentonboarding.application.service.JobApplicationService;
import com.wfm.experts.hr.recruitmentonboarding.entity.Job;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;
import com.wfm.experts.hr.recruitmentonboarding.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationMapper jobApplicationMapper;
    private final JobRepository jobRepository;

    @Override
    public JobApplicationDto applyForJob(JobApplicationDto dto) {
        // ✅ Validate job existence
        Job job = jobRepository.findById(dto.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + dto.getJobId()));

        // ✅ Optional: Block applications to closed jobs
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("This job is no longer accepting applications.");
        }

        JobApplication entity = jobApplicationMapper.toEntity(dto);
        JobApplication saved = repository.save(entity);
        return jobApplicationMapper.toDto(saved);
    }

    @Override
    public List<JobApplicationDto> getApplicationsByJobId(Long jobId) {
        return repository.findByJobId(jobId).stream()
                .map(jobApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public JobApplicationDto getApplicationById(Long id) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        return jobApplicationMapper.toDto(application);
    }
}
