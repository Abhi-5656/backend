package com.wfm.experts.setup.wfm.holiday.service.impl;

import com.wfm.experts.setup.wfm.holiday.dto.HolidayDTO;
import com.wfm.experts.setup.wfm.holiday.entity.Holiday;
import com.wfm.experts.setup.wfm.holiday.mapper.HolidayMapper;
import com.wfm.experts.setup.wfm.holiday.repository.HolidayRepository;
import com.wfm.experts.setup.wfm.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    @Override
    public HolidayDTO createHoliday(HolidayDTO holidayDTO) {
        Holiday entity = holidayMapper.toEntity(holidayDTO);
        Holiday saved = holidayRepository.save(entity);
        return holidayMapper.toDto(saved);
    }

    @Override
    public HolidayDTO updateHoliday(Long id, HolidayDTO holidayDTO) {
        Holiday existing = holidayRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Holiday not found with id " + id));
        Holiday updatedEntity = holidayMapper.toEntity(holidayDTO);
        updatedEntity.setId(existing.getId());
        if (existing.getCreatedAt() != null) {
            updatedEntity.setCreatedAt(existing.getCreatedAt());
        }
        Holiday saved = holidayRepository.save(updatedEntity);
        return holidayMapper.toDto(saved);
    }

    @Override
    public HolidayDTO getHoliday(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Holiday not found with id " + id));
        return holidayMapper.toDto(holiday);
    }

    @Override
    public List<HolidayDTO> getAllHolidays() {
        return holidayRepository.findAll()
                .stream()
                .map(holidayMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new NoSuchElementException("Holiday not found with id " + id);
        }
        holidayRepository.deleteById(id);
    }

    /**
     * Create multiple holidays in a single transaction.
     *
     * @param holidayDTOs list of holidays to create
     * @return list of created HolidayDTOs with IDs
     */
    @Override
    public List<HolidayDTO> createHolidays(List<HolidayDTO> holidayDTOs) {
        List<Holiday> entities = holidayDTOs.stream()
                .map(holidayMapper::toEntity)
                .collect(Collectors.toList());
        List<Holiday> saved = holidayRepository.saveAll(entities);
        return saved.stream()
                .map(holidayMapper::toDto)
                .collect(Collectors.toList());
    }
}
