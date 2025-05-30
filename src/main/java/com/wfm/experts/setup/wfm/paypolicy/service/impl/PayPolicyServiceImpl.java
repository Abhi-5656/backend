package com.wfm.experts.setup.wfm.paypolicy.service.impl;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;
import com.wfm.experts.setup.wfm.paypolicy.entity.PayPolicy;
import com.wfm.experts.setup.wfm.paypolicy.mapper.PayPolicyMapper;
import com.wfm.experts.setup.wfm.paypolicy.repository.PayPolicyRepository;
import com.wfm.experts.setup.wfm.paypolicy.service.PayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PayPolicyServiceImpl implements PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final PayPolicyMapper payPolicyMapper;

    @Override
    public PayPolicyDTO create(PayPolicyDTO dto) {
        PayPolicy entity = payPolicyMapper.toEntity(dto);
        PayPolicy saved = payPolicyRepository.save(entity);
        return payPolicyMapper.toDto(saved);
    }

    @Override
    public PayPolicyDTO update(Long id, PayPolicyDTO dto) {
        PayPolicy existing = payPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayPolicy not found with id: " + id));
        PayPolicy updated = payPolicyMapper.toEntity(dto);
        updated.setId(id);
        PayPolicy saved = payPolicyRepository.save(updated);
        return payPolicyMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PayPolicyDTO getById(Long id) {
        PayPolicy entity = payPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayPolicy not found with id: " + id));
        return payPolicyMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayPolicyDTO> getAll() {
        List<PayPolicy> entities = payPolicyRepository.findAll();
        return entities.stream()
                .map(payPolicyMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        PayPolicy entity = payPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayPolicy not found with id: " + id));
        payPolicyRepository.delete(entity);
    }
}
