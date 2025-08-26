package com.wfm.experts.setup.wfm.paypolicy.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;
import com.wfm.experts.setup.wfm.paypolicy.service.PayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/pay-policies")
@RequiredArgsConstructor
public class PayPolicyController extends WfmSetupController {

    private final PayPolicyService payPolicyService;

    @PostMapping
    public ResponseEntity<PayPolicyDTO> create(@Valid @RequestBody PayPolicyDTO dto) {
        PayPolicyDTO created = payPolicyService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayPolicyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PayPolicyDTO dto) {
        PayPolicyDTO updated = payPolicyService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayPolicyDTO> getById(@PathVariable Long id) {
        PayPolicyDTO policy = payPolicyService.getById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping
    public ResponseEntity<List<PayPolicyDTO>> getAll() {
        List<PayPolicyDTO> policies = payPolicyService.getAll();
        return ResponseEntity.ok(policies);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payPolicyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
