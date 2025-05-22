package com.wfm.experts.setup.wfm.shift.service;

import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
import java.util.List;

public interface ShiftRotationService {
    ShiftRotationDTO create(ShiftRotationDTO dto);
    ShiftRotationDTO update(Long id, ShiftRotationDTO dto);
    ShiftRotationDTO get(Long id);
    List<ShiftRotationDTO> getAll();
    void delete(Long id);
}
