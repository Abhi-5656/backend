package com.wfm.experts.setup.wfm.requesttype.service.impl;

import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.mapper.RequestTypeMapper;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeService;
import com.wfm.experts.setup.wfm.requesttype.support.NotFoundException;
import com.wfm.experts.setup.wfm.requesttype.support.RequestTypeSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestTypeServiceImpl implements RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;
    private final RequestTypeMapper requestTypeMapper;

    @Override
    @Transactional
    public RequestTypeDTO create(RequestTypeDTO dto) {
        // MapStruct: new aggregate, children IDs ignored
        RequestType entity = requestTypeMapper.toNewEntity(dto);
        RequestType saved = requestTypeRepository.save(entity);
        return requestTypeMapper.toDto(saved);
    }

    @Override
    public Optional<RequestTypeDTO> findById(Long id) {
        return requestTypeRepository.findById(id).map(requestTypeMapper::toDto);
    }

    @Override
    public Page<RequestTypeDTO> list(LocalDate effectiveFrom,
                                     LocalDate effectiveTo,
                                     LocalDate activeOnDate,
                                     Pageable pageable) {
        Specification<RequestType> spec = where(RequestTypeSpecifications.effectiveDateFrom(effectiveFrom))
                .and(RequestTypeSpecifications.effectiveDateTo(effectiveTo))
                .and(RequestTypeSpecifications.activeOn(activeOnDate));

        return requestTypeRepository.findAll(spec, pageable).map(requestTypeMapper::toDto);
    }

    @Override
    @Transactional
    public RequestTypeDTO update(Long id, RequestTypeDTO dto) {
        RequestType existing = requestTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestType not found: " + id));

        // Put semantics: we expect all sections present; updateEntity ignores nulls per MapperConfig
        // If you want strict PUT (reject nulls), validate dto before mapping.
        requestTypeMapper.updateEntity(existing, dto);

        RequestType saved = requestTypeRepository.save(existing);
        return requestTypeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RequestTypeDTO patch(Long id, RequestTypeDTO dto) {
        RequestType existing = requestTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestType not found: " + id));

        // Patch semantics: only non-null fields are applied (per MapperConfig IGNORE)
        requestTypeMapper.updateEntity(existing, dto);

        RequestType saved = requestTypeRepository.save(existing);
        return requestTypeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RequestType existing = requestTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestType not found: " + id));

        // Because RequestType -> configs is @OneToOne(cascade=ALL, orphanRemoval=true),
        // deleting parent will remove children.
        requestTypeRepository.delete(existing);
    }

    @Override
    public boolean exists(Long id) {
        return requestTypeRepository.existsById(id);
    }
}
