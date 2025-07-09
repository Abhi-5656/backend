// ConditionalRuleServiceImpl.java
package com.wfm.experts.setup.wfm.leavepolicy.service.impl;

import com.wfm.experts.setup.wfm.leavepolicy.dto.ConditionalRuleDTO;
import com.wfm.experts.setup.wfm.leavepolicy.entity.ConditionalRule;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.exception.ConditionalRuleNotFoundException;
import com.wfm.experts.setup.wfm.leavepolicy.exception.LeavePolicyNotFoundException;
import com.wfm.experts.setup.wfm.leavepolicy.mapper.ConditionalRuleMapper;
import com.wfm.experts.setup.wfm.leavepolicy.repository.ConditionalRuleRepository;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.ConditionalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConditionalRuleServiceImpl implements ConditionalRuleService {

    private final ConditionalRuleRepository ruleRepo;
    private final LeavePolicyRepository policyRepo;
    private final ConditionalRuleMapper mapper;

    @Override
    public ConditionalRuleDTO createForPolicy(Long policyId, ConditionalRuleDTO dto) {
        LeavePolicy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new LeavePolicyNotFoundException(policyId));

        ConditionalRule rule = mapper.toEntity(dto);
        rule.setLeavePolicy(policy);
        ConditionalRule saved = ruleRepo.save(rule);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConditionalRuleDTO getById(Long id) {
        ConditionalRule rule = ruleRepo.findById(id)
                .orElseThrow(() -> new ConditionalRuleNotFoundException(id));
        return mapper.toDto(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionalRuleDTO> getByPolicyId(Long policyId) {
        if (!policyRepo.existsById(policyId)) {
            throw new LeavePolicyNotFoundException(policyId);
        }
        return ruleRepo.findByLeavePolicyId(policyId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ConditionalRuleDTO update(Long id, ConditionalRuleDTO dto) {
        ConditionalRule existing = ruleRepo.findById(id)
                .orElseThrow(() -> new ConditionalRuleNotFoundException(id));

        ConditionalRule updated = mapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setLeavePolicy(existing.getLeavePolicy());

        ConditionalRule saved = ruleRepo.save(updated);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        ConditionalRule existing = ruleRepo.findById(id)
                .orElseThrow(() -> new ConditionalRuleNotFoundException(id));
        ruleRepo.delete(existing);
    }
}
