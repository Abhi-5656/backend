package com.wfm.experts.setup.orgstructure.service.impl;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.mapper.JobTitleMapper;
import com.wfm.experts.setup.orgstructure.repository.JobTitleRepository;
import com.wfm.experts.setup.orgstructure.service.JobTitleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobTitleServiceImpl implements JobTitleService {

    private final JobTitleRepository repository;
    private final JobTitleMapper mapper;

    @Override
    public JobTitleDto create(JobTitleDto dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @Override
    public JobTitleDto update(Long id, JobTitleDto dto) {
        JobTitle existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job Title not found"));

        existing.setJobTitle(dto.getJobTitle());
        existing.setShortName(dto.getShortName());
        existing.setCode(dto.getCode());
        existing.setSortOrder(dto.getSortOrder());
        existing.setColor(dto.getColor());
        existing.setEffectiveDate(dto.getEffectiveDate());
        existing.setExpirationDate(dto.getExpirationDate());

        return mapper.toDto(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public JobTitleDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Job Title not found"));
    }

    @Override
    public List<JobTitleDto> getAll() {
        return mapper.toDtoList(repository.findAll());
    }
}
