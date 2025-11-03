package com.wfm.experts.setup.wfm.holiday.service.impl;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayProfileDTO;
import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import com.wfm.experts.setup.wfm.holiday.exception.HolidayNotFoundException;
import com.wfm.experts.setup.wfm.holiday.exception.HolidayProfileAlreadyExistsException;
import com.wfm.experts.setup.wfm.holiday.exception.HolidayProfileNotFoundException;
import com.wfm.experts.setup.wfm.holiday.mapper.HolidayProfileMapper;
import com.wfm.experts.setup.wfm.holiday.repository.HolidayProfileRepository;
import com.wfm.experts.setup.wfm.holiday.repository.HolidayRepository;
import com.wfm.experts.setup.wfm.holiday.service.HolidayProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayProfileServiceImpl implements HolidayProfileService {

    private final HolidayProfileRepository holidayProfileRepository;
    private final HolidayProfileMapper holidayProfileMapper;
    private final HolidayRepository holidayRepository;

    @Override
    public HolidayProfileDTO createProfile(HolidayProfileDTO dto) {
        if (holidayProfileRepository.existsByProfileName(dto.getProfileName())) {
            throw new HolidayProfileAlreadyExistsException("A holiday profile with the name '" + dto.getProfileName() + "' already exists.");
        }
        HolidayProfile entity = holidayProfileMapper.toEntity(dto);

        // Validate that all provided holidays exist
        if (dto.getHolidays() != null && !dto.getHolidays().isEmpty()) {
            List<Long> holidayIds = dto.getHolidays().stream()
                    .map(Holiday::getId)
                    .collect(Collectors.toList());
            List<Holiday> foundHolidays = holidayRepository.findAllById(holidayIds);
            if (foundHolidays.size() != holidayIds.size()) {
                List<Long> foundIds = foundHolidays.stream().map(Holiday::getId).collect(Collectors.toList());
                holidayIds.removeAll(foundIds);
                throw new HolidayNotFoundException("Cannot create profile. Holiday not found with id: " + holidayIds.get(0));
            }
            entity.setHolidays(foundHolidays);
        }

        HolidayProfile saved = holidayProfileRepository.save(entity);
        return holidayProfileMapper.toDto(saved);
    }

    @Override
    public HolidayProfileDTO updateProfile(Long id, HolidayProfileDTO dto) {
        HolidayProfile existing = holidayProfileRepository.findById(id)
                .orElseThrow(() -> new HolidayProfileNotFoundException(id));

        holidayProfileRepository.findByProfileName(dto.getProfileName()).ifPresent(profile -> {
            if (!profile.getId().equals(id)) {
                throw new HolidayProfileAlreadyExistsException("A holiday profile with the name '" + dto.getProfileName() + "' already exists.");
            }
        });

        HolidayProfile updatedEntity = holidayProfileMapper.toEntity(dto);
        updatedEntity.setId(existing.getId());
        if (existing.getCreatedAt() != null) {
            updatedEntity.setCreatedAt(existing.getCreatedAt());
        }
        HolidayProfile saved = holidayProfileRepository.save(updatedEntity);
        return holidayProfileMapper.toDto(saved);
    }

    @Override
    public HolidayProfileDTO getProfile(Long id) {
        HolidayProfile profile = holidayProfileRepository.findById(id)
                .orElseThrow(() -> new HolidayProfileNotFoundException(id));
        return holidayProfileMapper.toDto(profile);
    }

    @Override
    public List<HolidayProfileDTO> getAllProfiles() {
        return holidayProfileRepository.findAll()
                .stream()
                .map(holidayProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProfile(Long id) {
        if (!holidayProfileRepository.existsById(id)) {
            throw new HolidayProfileNotFoundException(id);
        }
        holidayProfileRepository.deleteById(id);
    }
}