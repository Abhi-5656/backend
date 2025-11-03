package com.wfm.experts.setup.wfm.paypolicy.controller;

import com.wfm.experts.setup.wfm.controller.WfmSetupController;
import com.wfm.experts.setup.wfm.paypolicy.dto.PayPolicyDTO;
import com.wfm.experts.setup.wfm.paypolicy.service.PayPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setup/wfm/pay-policies")
@RequiredArgsConstructor
@Validated
public class PayPolicyController extends WfmSetupController {

    private final PayPolicyService payPolicyService;

    @PostMapping
    @PreAuthorize("hasAuthority('wfm:setup:pay-policy:create')")
    public ResponseEntity<PayPolicyDTO> create(@Valid @RequestBody PayPolicyDTO dto) {
        PayPolicyDTO created = payPolicyService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:pay-policy:update')")
    public ResponseEntity<PayPolicyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PayPolicyDTO dto) {
        PayPolicyDTO updated = payPolicyService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:pay-policy:read')")
    public ResponseEntity<PayPolicyDTO> getById(@PathVariable Long id) {
        PayPolicyDTO policy = payPolicyService.getById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('wfm:setup:pay-policy:read')")
    public ResponseEntity<List<PayPolicyDTO>> getAll() {
        List<PayPolicyDTO> policies = payPolicyService.getAll();
        return ResponseEntity.ok(policies);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wfm:setup:pay-policy:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payPolicyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}