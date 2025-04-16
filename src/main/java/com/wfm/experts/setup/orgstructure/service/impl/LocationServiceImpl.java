package com.wfm.experts.setup.orgstructure.service.impl;

import com.wfm.experts.setup.orgstructure.dto.JobTitleDto;
import com.wfm.experts.setup.orgstructure.dto.LocationDto;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import com.wfm.experts.setup.orgstructure.entity.Location;
import com.wfm.experts.setup.orgstructure.exception.ResourceNotFoundException;
import com.wfm.experts.setup.orgstructure.mapper.LocationMapper;
import com.wfm.experts.setup.orgstructure.repository.BusinessUnitRepository;
import com.wfm.experts.setup.orgstructure.repository.JobTitleRepository;
import com.wfm.experts.setup.orgstructure.repository.LocationRepository;
import com.wfm.experts.setup.orgstructure.service.LocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final JobTitleRepository jobTitleRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public LocationDto create(LocationDto dto) {
        Location location = locationMapper.toEntity(dto);

        BusinessUnit businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found"));
        location.setBusinessUnit(businessUnit);

        if (dto.getParentId() != null) {
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Location not found"));
            location.setParent(parent);
            location.setRoot(false);
        } else {
            location.setParent(null);
            location.setRoot(true);
        }

        // Handle Job Titles
        if (dto.getJobTitles() != null && !dto.getJobTitles().isEmpty()) {
            List<Long> jobTitleIds = dto.getJobTitles().stream()
                    .map(JobTitleDto::getId)
                    .collect(Collectors.toList());
            List<JobTitle> jobTitles = jobTitleRepository.findAllById(jobTitleIds);
            location.setJobTitles(jobTitles);
        } else {
            location.setJobTitles(Collections.emptyList());
        }

        return locationMapper.toDtoWithChildren(locationRepository.save(location));
    }

    @Override
    public LocationDto getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return locationMapper.toDtoWithChildren(location);
    }

    @Override
    public List<LocationDto> getAll() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .filter(loc -> loc.getParent() == null)
                .map(locationMapper::toDtoWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LocationDto update(Long id, LocationDto dto) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        existing.setName(dto.getName());
        existing.setColor(dto.getColor());
        existing.setEffectiveDate(dto.getEffectiveDate());
        existing.setExpirationDate(dto.getExpirationDate());

        if (dto.getParentId() != null && !dto.getParentId().equals(id)) {
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Location not found"));
            existing.setParent(parent);
            existing.setRoot(false);
        } else {
            existing.setParent(null);
            existing.setRoot(true);
        }

        // Update Job Titles
        if (dto.getJobTitles() != null) {
            List<Long> jobTitleIds = dto.getJobTitles().stream()
                    .map(JobTitleDto::getId)
                    .collect(Collectors.toList());
            List<JobTitle> jobTitles = jobTitleRepository.findAllById(jobTitleIds);
            existing.setJobTitles(jobTitles);
        }

        return locationMapper.toDtoWithChildren(locationRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LocationDto assignJobTitle(Long locationId, Long jobTitleId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        JobTitle jobTitle = jobTitleRepository.findById(jobTitleId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Title not found"));

        if (location.getJobTitles() == null) {
            location.setJobTitles(new ArrayList<>());
        }

        // Only add if not already present
        boolean alreadyAssigned = location.getJobTitles().stream()
                .anyMatch(j -> j.getId().equals(jobTitleId));
        if (!alreadyAssigned) {
            location.getJobTitles().add(jobTitle);
        }

        return locationMapper.toDtoWithChildren(locationRepository.save(location));
    }

    @Override
    @Transactional
    public LocationDto removeJobTitle(Long locationId, Long jobTitleId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (location.getJobTitles() != null) {
            location.getJobTitles().removeIf(job -> job.getId().equals(jobTitleId));
        }

        return locationMapper.toDtoWithChildren(locationRepository.save(location));
    }
}
