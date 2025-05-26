package com.wfm.experts.setup.wfm.holiday.service.impl;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayProfileDTO;
import com.wfm.experts.setup.wfm.holiday.entity.HolidayProfile;
import com.wfm.experts.setup.wfm.holiday.exception.HolidayProfileNotFoundException;
import com.wfm.experts.setup.wfm.holiday.mapper.HolidayProfileMapper;
import com.wfm.experts.setup.wfm.holiday.repository.HolidayProfileRepository;
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

    @Override
    public HolidayProfileDTO createProfile(HolidayProfileDTO dto) {
        HolidayProfile entity = holidayProfileMapper.toEntity(dto);
        HolidayProfile saved = holidayProfileRepository.save(entity);
        return holidayProfileMapper.toDto(saved);
    }

    @Override
    public HolidayProfileDTO updateProfile(Long id, HolidayProfileDTO dto) {
        HolidayProfile existing = holidayProfileRepository.findById(id)
                .orElseThrow(() -> new HolidayProfileNotFoundException(id));
        HolidayProfile updatedEntity = holidayProfileMapper.toEntity(dto);
        updatedEntity.setId(existing.getId());
        // Retain createdAt if your entity/DTO handles it
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
