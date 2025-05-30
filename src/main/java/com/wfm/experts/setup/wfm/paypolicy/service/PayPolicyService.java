package com.wfm.experts.setup.wfm.paypolicy.service;

import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;

import java.util.List;

public interface PayPolicyService {
    PayPolicyDTO create(PayPolicyDTO dto);
    PayPolicyDTO update(Long id, PayPolicyDTO dto);
    PayPolicyDTO getById(Long id);
    List<PayPolicyDTO> getAll();
    void delete(Long id);
}
