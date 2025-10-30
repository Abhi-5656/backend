//package com.wfm.experts.setup.wfm.shift.service.impl;
//
//import com.wfm.experts.setup.wfm.shift.dto.*;
//import com.wfm.experts.setup.wfm.shift.entity.*;
//import com.wfm.experts.setup.wfm.shift.repository.*;
//import com.wfm.experts.setup.wfm.shift.mapper.ShiftRotationMapper;
//import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
//import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ShiftRotationServiceImpl implements ShiftRotationService {
//
//    private final ShiftRotationRepository shiftRotationRepo;
//    private final ShiftRotationDayRepository shiftRotationDayRepo;
//    private final ShiftRepository shiftRepo;
//    private final ShiftRotationMapper shiftRotationMapper;
//
//    @Override
//    public ShiftRotationDTO create(ShiftRotationDTO dto) {
//        ShiftRotation shiftRotation = shiftRotationMapper.toEntity(dto);
//        shiftRotation = shiftRotationRepo.save(shiftRotation);
//
//        if (dto.getWeeksPattern() != null) {
//            for (WeekPatternDTO weekPattern : dto.getWeeksPattern()) {
//                Integer weekNo = weekPattern.getWeek();
//                if (weekPattern.getDays() != null) {
//                    for (ShiftRotationDayDTO dayDTO : weekPattern.getDays()) {
//                        Shift shift = null;
//                        if (dayDTO.getShift() != null && !Boolean.TRUE.equals(dayDTO.getWeekOff())) {
//                            shift = shiftRepo.findById(dayDTO.getShift().getId())
//                                    .orElseThrow(() -> new RuntimeException("Shift not found for day: " + dayDTO.getWeekday()));
//                        }
//                        ShiftRotationDay day = ShiftRotationDay.builder()
//                                .shiftRotation(shiftRotation)
//                                .week(weekNo)
//                                .weekday(dayDTO.getWeekday())
//                                .shift(shift)
//                                .weekOff(Boolean.TRUE.equals(dayDTO.getWeekOff()))
//                                .build();
//                        shiftRotationDayRepo.save(day);
//                    }
//                }
//            }
//        }
//        return get(shiftRotation.getId());
//    }
//
//    @Override
//    public ShiftRotationDTO update(Long id, ShiftRotationDTO dto) {
//        ShiftRotation rotation = shiftRotationRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("ShiftRotation not found"));
//        shiftRotationMapper.updateEntityFromDto(dto, rotation);
//        rotation = shiftRotationRepo.save(rotation);
//
//        List<ShiftRotationDay> existingDays = shiftRotationDayRepo.findByShiftRotationId(id);
//        shiftRotationDayRepo.deleteAll(existingDays);
//
//        if (dto.getWeeksPattern() != null) {
//            for (WeekPatternDTO weekPattern : dto.getWeeksPattern()) {
//                Integer weekNo = weekPattern.getWeek();
//                if (weekPattern.getDays() != null) {
//                    for (ShiftRotationDayDTO dayDTO : weekPattern.getDays()) {
//                        Shift shift = null;
//                        if (dayDTO.getShift() != null && !Boolean.TRUE.equals(dayDTO.getWeekOff())) {
//                            shift = shiftRepo.findById(dayDTO.getShift().getId())
//                                    .orElseThrow(() -> new RuntimeException("Shift not found for day: " + dayDTO.getWeekday()));
//                        }
//                        ShiftRotationDay day = ShiftRotationDay.builder()
//                                .shiftRotation(rotation)
//                                .week(weekNo)
//                                .weekday(dayDTO.getWeekday())
//                                .shift(shift)
//                                .weekOff(Boolean.TRUE.equals(dayDTO.getWeekOff()))
//                                .build();
//                        shiftRotationDayRepo.save(day);
//                    }
//                }
//            }
//        }
//        return get(rotation.getId());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public ShiftRotationDTO get(Long id) {
//        ShiftRotation rotation = shiftRotationRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("ShiftRotation not found"));
//
//        List<ShiftRotationDay> days = shiftRotationDayRepo.findByShiftRotationId(id);
//        Map<Integer, List<ShiftRotationDayDTO>> weekMap = new HashMap<>();
//        for (ShiftRotationDay day : days) {
//            ShiftRotationDayDTO dayDTO = shiftRotationMapper.toDayDto(day);
//            // Handle null shift for week off
//            if (Boolean.TRUE.equals(day.getWeekOff())) {
//                dayDTO.setWeekOff(true);
//                dayDTO.setShift(null);
//            }
//            weekMap.computeIfAbsent(day.getWeek(), k -> new ArrayList<>()).add(dayDTO);
//        }
//        List<WeekPatternDTO> weeksPattern = new ArrayList<>();
//        weekMap.keySet().stream().sorted().forEach(weekNo -> {
//            weeksPattern.add(WeekPatternDTO.builder().week(weekNo).days(weekMap.get(weekNo)).build());
//        });
//
//        ShiftRotationDTO dto = shiftRotationMapper.toDto(rotation);
//        dto.setWeeksPattern(weeksPattern);
//        return dto;
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ShiftRotationDTO> getAll() {
//        List<ShiftRotation> all = shiftRotationRepo.findAll();
//        List<ShiftRotationDTO> result = new ArrayList<>();
//        for (ShiftRotation rotation : all) {
//            result.add(get(rotation.getId()));
//        }
//        return result;
//    }
//
//    @Override
//    public void delete(Long id) {
//        List<ShiftRotationDay> days = shiftRotationDayRepo.findByShiftRotationId(id);
//        shiftRotationDayRepo.deleteAll(days);
//        shiftRotationRepo.deleteById(id);
//    }
//}

package com.wfm.experts.setup.wfm.shift.service.impl;

import com.wfm.experts.setup.wfm.shift.dto.*;
import com.wfm.experts.setup.wfm.shift.entity.*;
import com.wfm.experts.setup.wfm.shift.exception.ShiftNotFoundException;
import com.wfm.experts.setup.wfm.shift.exception.ShiftRotationAlreadyExistsException;
import com.wfm.experts.setup.wfm.shift.exception.ShiftRotationNotFoundException;
import com.wfm.experts.setup.wfm.shift.repository.*;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftRotationMapper;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftRotationServiceImpl implements ShiftRotationService {

    private final ShiftRotationRepository shiftRotationRepo;
    private final ShiftRotationDayRepository shiftRotationDayRepo;
    private final ShiftRepository shiftRepo;
    private final ShiftRotationMapper shiftRotationMapper;

    @Override
    public ShiftRotationDTO create(ShiftRotationDTO dto) {
        if (shiftRotationRepo.findByName(dto.getName()).isPresent()) {
            throw new ShiftRotationAlreadyExistsException(dto.getName());
        }
        ShiftRotation shiftRotation = shiftRotationMapper.toEntity(dto);
        shiftRotation = shiftRotationRepo.save(shiftRotation);

        if (dto.getWeeksPattern() != null) {
            for (WeekPatternDTO weekPattern : dto.getWeeksPattern()) {
                Integer weekNo = weekPattern.getWeek();
                if (weekPattern.getDays() != null) {
                    for (ShiftRotationDayDTO dayDTO : weekPattern.getDays()) {
                        Shift shift = null;
                        if (dayDTO.getShift() != null && !Boolean.TRUE.equals(dayDTO.getWeekOff())) {
                            shift = shiftRepo.findById(dayDTO.getShift().getId())
                                    .orElseThrow(() -> new ShiftNotFoundException("Shift not found for day: " + dayDTO.getWeekday()));
                        }
                        ShiftRotationDay day = ShiftRotationDay.builder()
                                .shiftRotation(shiftRotation)
                                .week(weekNo)
                                .weekday(dayDTO.getWeekday())
                                .shift(shift)
                                .weekOff(Boolean.TRUE.equals(dayDTO.getWeekOff()))
                                .build();
                        shiftRotationDayRepo.save(day);
                    }
                }
            }
        }
        return get(shiftRotation.getId());
    }

    @Override
    public ShiftRotationDTO update(Long id, ShiftRotationDTO dto) {
        ShiftRotation rotation = shiftRotationRepo.findById(id)
                .orElseThrow(() -> new ShiftRotationNotFoundException("ShiftRotation not found with id: " + id));
        shiftRotationMapper.updateEntityFromDto(dto, rotation);
        rotation = shiftRotationRepo.save(rotation);

        List<ShiftRotationDay> existingDays = shiftRotationDayRepo.findByShiftRotationId(id);
        shiftRotationDayRepo.deleteAll(existingDays);

        if (dto.getWeeksPattern() != null) {
            for (WeekPatternDTO weekPattern : dto.getWeeksPattern()) {
                Integer weekNo = weekPattern.getWeek();
                if (weekPattern.getDays() != null) {
                    for (ShiftRotationDayDTO dayDTO : weekPattern.getDays()) {
                        Shift shift = null;
                        if (dayDTO.getShift() != null && !Boolean.TRUE.equals(dayDTO.getWeekOff())) {
                            shift = shiftRepo.findById(dayDTO.getShift().getId())
                                    .orElseThrow(() -> new ShiftNotFoundException("Shift not found for day: " + dayDTO.getWeekday()));
                        }
                        ShiftRotationDay day = ShiftRotationDay.builder()
                                .shiftRotation(rotation)
                                .week(weekNo)
                                .weekday(dayDTO.getWeekday())
                                .shift(shift)
                                .weekOff(Boolean.TRUE.equals(dayDTO.getWeekOff()))
                                .build();
                        shiftRotationDayRepo.save(day);
                    }
                }
            }
        }
        return get(rotation.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftRotationDTO get(Long id) {
        ShiftRotation rotation = shiftRotationRepo.findById(id)
                .orElseThrow(() -> new ShiftRotationNotFoundException("ShiftRotation not found with id: " + id));

        List<ShiftRotationDay> days = shiftRotationDayRepo.findByShiftRotationId(id);
        Map<Integer, List<ShiftRotationDayDTO>> weekMap = new HashMap<>();
        for (ShiftRotationDay day : days) {
            ShiftRotationDayDTO dayDTO = shiftRotationMapper.toDayDto(day);
            // Handle null shift for week off
            if (Boolean.TRUE.equals(day.getWeekOff())) {
                dayDTO.setWeekOff(true);
                dayDTO.setShift(null);
            }
            weekMap.computeIfAbsent(day.getWeek(), k -> new ArrayList<>()).add(dayDTO);
        }
        List<WeekPatternDTO> weeksPattern = new ArrayList<>();
        weekMap.keySet().stream().sorted().forEach(weekNo -> {
            weeksPattern.add(WeekPatternDTO.builder().week(weekNo).days(weekMap.get(weekNo)).build());
        });

        ShiftRotationDTO dto = shiftRotationMapper.toDto(rotation);
        dto.setWeeksPattern(weeksPattern);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRotationDTO> getAll() {
        List<ShiftRotation> all = shiftRotationRepo.findAll();
        List<ShiftRotationDTO> result = new ArrayList<>();
        for (ShiftRotation rotation : all) {
            result.add(get(rotation.getId()));
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        List<ShiftRotationDay> days = shiftRotationDayRepo.findByShiftRotationId(id);
        shiftRotationDayRepo.deleteAll(days);
        shiftRotationRepo.deleteById(id);
    }
}