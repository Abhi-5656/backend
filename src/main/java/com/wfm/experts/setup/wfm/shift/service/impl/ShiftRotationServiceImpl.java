package com.wfm.experts.setup.wfm.shift.service.impl;

import com.wfm.experts.setup.wfm.shift.dto.ShiftRotationDTO;
import com.wfm.experts.setup.wfm.shift.entity.ShiftRotation;
import com.wfm.experts.setup.wfm.shift.exception.ShiftRotationNotFoundException;
import com.wfm.experts.setup.wfm.shift.repository.ShiftRotationRepository;
import com.wfm.experts.setup.wfm.shift.service.ShiftRotationService;
import com.wfm.experts.setup.wfm.shift.mapper.ShiftRotationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftRotationServiceImpl implements ShiftRotationService {

    private final ShiftRotationRepository repository;
    private final ShiftRotationMapper mapper;

    @Override
    public ShiftRotationDTO create(ShiftRotationDTO dto) {
        ShiftRotation entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public ShiftRotationDTO update(Long id, ShiftRotationDTO dto) {
        ShiftRotation existing = repository.findById(id)
                .orElseThrow(() -> new ShiftRotationNotFoundException(id));
        mapper.updateEntityFromDto(dto, existing);
        ShiftRotation updated = repository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    public ShiftRotationDTO get(Long id) {
        ShiftRotation entity = repository.findById(id)
                .orElseThrow(() -> new ShiftRotationNotFoundException(id));
        return mapper.toDto(entity);
    }

    @Override
    public List<ShiftRotationDTO> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ShiftRotationNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
