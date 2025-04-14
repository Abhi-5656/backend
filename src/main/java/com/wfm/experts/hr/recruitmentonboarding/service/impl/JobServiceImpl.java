package com.wfm.experts.hr.recruitmentonboarding.service.impl;

import com.wfm.experts.hr.recruitmentonboarding.dto.JobDto;
import com.wfm.experts.hr.recruitmentonboarding.entity.Job;
import com.wfm.experts.hr.recruitmentonboarding.enums.ExperienceLevel;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;
import com.wfm.experts.hr.recruitmentonboarding.mapper.JobMapper;
import com.wfm.experts.hr.recruitmentonboarding.repository.JobRepository;
import com.wfm.experts.hr.recruitmentonboarding.service.JobService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobServiceImpl(JobRepository jobRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    @Override
    public JobDto create(JobDto jobDto) {
        Job job = jobMapper.toEntity(jobDto);
        return jobMapper.toDto(jobRepository.save(job));
    }

    @Override
    public JobDto update(Long id, JobDto jobDto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id " + id));

        // Update fields
        job.setTitle(jobDto.getTitle());
        job.setPosition(jobDto.getPosition());
        job.setDescription(jobDto.getDescription());
        job.setOpenings(jobDto.getOpenings());
        job.setAnnualSalary(jobDto.getAnnualSalary());
        job.setEmploymentType(jobDto.getEmploymentType());
        job.setExperienceLevel(jobDto.getExperienceLevel());
        job.setYearsOfExperience(jobDto.getYearsOfExperience());
        job.setStatus(jobDto.getStatus());
        job.setExpiryDate(jobDto.getExpiryDate());

        return jobMapper.toDto(jobRepository.save(job));
    }

    @Override
    public Optional<JobDto> getById(Long id) {
        return jobRepository.findById(id).map(jobMapper::toDto);
    }

    @Override
    public List<JobDto> getAll() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new EntityNotFoundException("Job not found with id " + id);
        }
        jobRepository.deleteById(id);
    }

    @Override
    public List<JobDto> getByStatus(JobStatus status) {
        return jobRepository.findByStatus(status)
                .stream()
                .map(jobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobDto> getByExperienceLevel(ExperienceLevel experienceLevel) {
        return jobRepository.findByExperienceLevel(experienceLevel)
                .stream()
                .map(jobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobDto> searchByTitle(String keyword) {
        return jobRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(jobMapper::toDto)
                .collect(Collectors.toList());
    }
}
