// LeavePolicyServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ConditionalRule;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeavePolicyNotFoundException;
import com.wfm.experts.setup.wfm.leavepolicy.mapper.LeavePolicyMapper;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeavePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeavePolicyServiceImpl implements LeavePolicyService {

    private final LeavePolicyRepository policyRepo;
    private final LeavePolicyMapper mapper;

    @Override
    public LeavePolicyDTO create(LeavePolicyDTO dto) {
        LeavePolicy policy = mapper.toEntity(dto);
        // ensure any child rules have back-reference
        if (policy.getConditionalRules() != null) {
            policy.getConditionalRules().forEach(rule -> rule.setLeavePolicy(policy));
        }
        LeavePolicy saved = policyRepo.save(policy);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyDTO getById(Long id) {
        LeavePolicy policy = policyRepo.findById(id)
                .orElseThrow(() -> new LeavePolicyNotFoundException(id));
        return mapper.toDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public LeavePolicyDTO getByCode(String code) {
        LeavePolicy policy = policyRepo.findByCode(code)
                .orElseThrow(() -> new LeavePolicyNotFoundException(code, true));
        return mapper.toDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeavePolicyDTO> getAll() {
        return policyRepo.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LeavePolicyDTO update(Long id, LeavePolicyDTO dto) {
        LeavePolicy existing = policyRepo.findById(id)
                .orElseThrow(() -> new LeavePolicyNotFoundException(id));

        // map all updatable fields
        LeavePolicy updated = mapper.toEntity(dto);
        updated.setId(existing.getId());

        // preserve orphanRemoval: clear existing children, then reattach from dto
        existing.getConditionalRules().clear();
        if (updated.getConditionalRules() != null) {
            updated.getConditionalRules()
                    .forEach(rule -> rule.setLeavePolicy(updated));
        }

        LeavePolicy saved = policyRepo.save(updated);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        LeavePolicy existing = policyRepo.findById(id)
                .orElseThrow(() -> new LeavePolicyNotFoundException(id));
        policyRepo.delete(existing);
    }
}
