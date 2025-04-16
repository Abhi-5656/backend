package com.wfm.experts.setup.orgstructure.service.impl;

import com.wfm.experts.setup.orgstructure.dto.BusinessUnitDto;
import com.wfm.experts.setup.orgstructure.entity.BusinessUnit;
import com.wfm.experts.setup.orgstructure.mapper.BusinessUnitMapper;
import com.wfm.experts.setup.orgstructure.repository.BusinessUnitRepository;
import com.wfm.experts.setup.orgstructure.service.BusinessUnitService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessUnitServiceImpl implements BusinessUnitService {

    private final BusinessUnitRepository repository;
    private final BusinessUnitMapper businessUnitMapper;

    @Override
    public BusinessUnitDto create(BusinessUnitDto dto) {
        BusinessUnit entity = businessUnitMapper.toEntity(dto);
        return businessUnitMapper.toDto(repository.save(entity));
    }

    @Override
    public BusinessUnitDto update(Long id, BusinessUnitDto dto) {
        BusinessUnit existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business Unit not found"));

        existing.setName(dto.getName());
        existing.setColor(dto.getColor());
        existing.setEffectiveDate(dto.getEffectiveDate());
        existing.setExpirationDate(dto.getExpirationDate());

        return businessUnitMapper.toDto(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public BusinessUnitDto getById(Long id) {
        return repository.findById(id)
                .map(businessUnitMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Business Unit not found"));
    }

    @Override
    public List<BusinessUnitDto> getAll() {
        return businessUnitMapper.toDtoList(repository.findAll());
    }
}
