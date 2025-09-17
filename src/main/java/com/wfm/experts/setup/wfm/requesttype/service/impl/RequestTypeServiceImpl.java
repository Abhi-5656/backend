package com.wfm.experts.setup.wfm.requesttype.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.requesttype.dto.RequestTypeDTO;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.mapper.RequestTypeMapper;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
import com.wfm.experts.setup.wfm.requesttype.service.RequestTypeService;
import com.wfm.experts.setup.wfm.requesttype.support.NotFoundException;
import com.wfm.experts.setup.wfm.requesttype.support.RequestTypeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class RequestTypeServiceImpl implements RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;
    private final RequestTypeMapper requestTypeMapper;
    private final LeavePolicyRepository leavePolicyRepository;

    public RequestTypeServiceImpl(RequestTypeRepository requestTypeRepository, RequestTypeMapper requestTypeMapper, LeavePolicyRepository leavePolicyRepository) {
        this.requestTypeRepository = requestTypeRepository;
        this.requestTypeMapper = requestTypeMapper;
        this.leavePolicyRepository = leavePolicyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RequestTypeDTO> findAll(String name, Date effectiveDate, Pageable pageable) {
        Specification<RequestType> spec = Specification.where(null);
        if (name != null && !name.isEmpty()) {
            spec = spec.and(RequestTypeSpecifications.nameContains(name));
        }
        if (effectiveDate != null) {
            spec = spec.and(RequestTypeSpecifications.effectiveDateEquals(effectiveDate));
        }
        return requestTypeRepository.findAll(spec, pageable).map(requestTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestTypeDTO findById(Long id) {
        return requestTypeRepository.findById(id)
                .map(requestTypeMapper::toDto)
                .orElseThrow(() -> new NotFoundException("RequestType not found with id: " + id));
    }

    @Override
    @Transactional
    public RequestTypeDTO create(RequestTypeDTO requestTypeDTO) {
        RequestType requestType = requestTypeMapper.toEntity(requestTypeDTO);

        if (requestTypeDTO.getLeavePolicy() != null && requestTypeDTO.getLeavePolicy().getId() != null) {
            LeavePolicy leavePolicy = leavePolicyRepository.findById(requestTypeDTO.getLeavePolicy().getId())
                    .orElseThrow(() -> new NotFoundException("LeavePolicy not found with id: " + requestTypeDTO.getLeavePolicy().getId()));
            requestType.setLeavePolicy(leavePolicy);
        }

        return requestTypeMapper.toDto(requestTypeRepository.save(requestType));
    }

    @Override
    @Transactional
    public RequestTypeDTO update(Long id, RequestTypeDTO requestTypeDTO) {
        RequestType existingRequestType = requestTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RequestType not found with id: " + id));

        // Use the mapper to update the entity, but ignore the ID from the DTO
        RequestType updatedRequestType = requestTypeMapper.toEntity(requestTypeDTO);
        updatedRequestType.setId(existingRequestType.getId());

        if (requestTypeDTO.getLeavePolicy() != null && requestTypeDTO.getLeavePolicy().getId() != null) {
            LeavePolicy leavePolicy = leavePolicyRepository.findById(requestTypeDTO.getLeavePolicy().getId())
                    .orElseThrow(() -> new NotFoundException("LeavePolicy not found with id: " + requestTypeDTO.getLeavePolicy().getId()));
            updatedRequestType.setLeavePolicy(leavePolicy);
        } else {
            updatedRequestType.setLeavePolicy(null);
        }

        return requestTypeMapper.toDto(requestTypeRepository.save(updatedRequestType));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!requestTypeRepository.existsById(id)) {
            throw new NotFoundException("RequestType not found with id: " + id);
        }
        requestTypeRepository.deleteById(id);
    }
}