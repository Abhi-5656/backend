package com.wfm.experts.setup.wfm.shift.service;

import com.wfm.experts.setup.wfm.shift.dto.ShiftDTO;
import java.util.List;

public interface ShiftService {
    ShiftDTO createShift(ShiftDTO dto);
    ShiftDTO updateShift(Long id, ShiftDTO dto);
    ShiftDTO getShift(Long id);
    List<ShiftDTO> getAllShifts();
    void deleteShift(Long id);
}
