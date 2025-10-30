//package com.wfm.experts.setup.wfm.shift.service.impl;
//
//import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
//import com.wfm.experts.setup.wfm.shift.entity.Shift;
//import com.wfm.experts.setup.wfm.shift.exception.ShiftNotFoundException;
//import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
//import com.wfm.experts.setup.wfm.shift.service.ShiftService;
//import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ShiftServiceImpl implements ShiftService {
//
//    private final ShiftRepository shiftRepository;
//    private final ShiftMapper shiftMapper;
//
//    @Override
//    public ShiftDTO createShift(ShiftDTO dto) {
//        Shift shift = shiftMapper.toEntity(dto);
//        shift = shiftRepository.save(shift);
//        return shiftMapper.toDto(shift);
//    }
//
//    @Override
//    public ShiftDTO updateShift(Long id, ShiftDTO dto) {
//        Shift existing = shiftRepository.findById(id)
//                .orElseThrow(() -> new ShiftNotFoundException(id));
//        shiftMapper.updateEntityFromDto(dto, existing);
//        Shift updated = shiftRepository.save(existing);
//        return shiftMapper.toDto(updated);
//    }
//
//    @Override
//    public ShiftDTO getShift(Long id) {
//        Shift shift = shiftRepository.findById(id)
//                .orElseThrow(() -> new ShiftNotFoundException(id));
//        return shiftMapper.toDto(shift);
//    }
//
//    @Override
//    public List<ShiftDTO> getAllShifts() {
//        return shiftRepository.findAll()
//                .stream().map(shiftMapper::toDto).toList();
//    }
//
//    @Override
//    public void deleteShift(Long id) {
//        if (!shiftRepository.existsById(id)) {
//            throw new ShiftNotFoundException(id);
//        }
//        shiftRepository.deleteById(id);
//    }
//}
package com.wfm.experts.setup.wfm.shift.service.impl;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import com.wfm.experts.setup.wfm.shift.entity.Shift;
import com.wfm.experts.setup.wfm.shift.exception.ShiftAlreadyExistsException;
import com.wfm.experts.setup.wfm.shift.exception.ShiftNotFoundException;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRepository;
import com.wfm.experts.setup.wfm.shift.service.ShiftService;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    @Override
    public ShiftDTO createShift(ShiftDTO dto) {
        if (shiftRepository.existsByShiftName(dto.getShiftName())) {
            throw new ShiftAlreadyExistsException(dto.getShiftName());
        }
        Shift shift = shiftMapper.toEntity(dto);
        shift = shiftRepository.save(shift);
        return shiftMapper.toDto(shift);
    }

    @Override
    public ShiftDTO updateShift(Long id, ShiftDTO dto) {
        Shift existing = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException("Shift not found with id: " + id));
        shiftMapper.updateEntityFromDto(dto, existing);
        Shift updated = shiftRepository.save(existing);
        return shiftMapper.toDto(updated);
    }

    @Override
    public ShiftDTO getShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException("Shift not found with id: " + id));
        return shiftMapper.toDto(shift);
    }

    @Override
    public List<ShiftDTO> getAllShifts() {
        return shiftRepository.findAll()
                .stream().map(shiftMapper::toDto).toList();
    }

    @Override
    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new ShiftNotFoundException("Shift not found with id: " + id);
        }
        shiftRepository.deleteById(id);
    }
}