package com.wfm.experts.setup.orgstructure.service.impl;

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

        // Fetch and set BusinessUnit
        BusinessUnit businessUnit = businessUnitRepository.findById(dto.getBusinessUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Business Unit not found"));
        location.setBusinessUnit(businessUnit);

        // Handle parent (root or child)
        if (dto.getParentId() != null) {
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Location not found"));
            location.setParent(parent);
            location.setRoot(false);
        } else {
            location.setParent(null);
            location.setRoot(true);
        }

        // Optional: assign Job Titles
        if (dto.getJobTitleIds() != null && !dto.getJobTitleIds().isEmpty()) {
            List<JobTitle> foundJobTitles = jobTitleRepository.findAllById(dto.getJobTitleIds());
            List<JobTitle> jobTitles = new ArrayList<>(foundJobTitles); // no conversion issue now
            location.setJobTitles(jobTitles);
        } else {
            location.setJobTitles(Collections.EMPTY_LIST);
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
                .filter(loc -> loc.getParent() == null) // return only top-level nodes
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

        // Update parent (if applicable)
        if (dto.getParentId() != null && !dto.getParentId().equals(id)) {
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Location not found"));
            existing.setParent(parent);
            existing.setRoot(false);
        } else {
            existing.setParent(null);
            existing.setRoot(true);
        }

        // Update job titles
        if (dto.getJobTitleIds() != null) {
            List<JobTitle> jobTitles = new ArrayList<>(jobTitleRepository.findAllById(dto.getJobTitleIds()));
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
}
