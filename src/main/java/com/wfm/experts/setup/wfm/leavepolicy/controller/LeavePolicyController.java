package com.wfm.experts.setup.wfm.leavepolicy.controller;

import com.wfm.experts.setup.wfm.leavepolicy.dto.LeavePolicyDto;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Leave Policies.
 * Exposes endpoints for CRUD operations.
 */
@RestController
@RequestMapping("/api/setup/wfm/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    /**
     * POST /api/setup/wfm/leave-policies : Create a new leave policy.
     *
     * @param leavePolicyDto the leave policy to create.
     * @return the ResponseEntity with status 201 (Created) and with body the new leavePolicyDto.
     */
    @PostMapping
    public ResponseEntity<LeavePolicyDto> createLeavePolicy(@Valid @RequestBody LeavePolicyDto leavePolicyDto) {
        LeavePolicyDto createdPolicy = leavePolicyService.createLeavePolicy(leavePolicyDto);
        return new ResponseEntity<>(createdPolicy, HttpStatus.CREATED);
    }

    /**
     * GET /api/setup/wfm/leave-policies : Get all leave policies.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of leave policies in body.
     */
    @GetMapping
    public ResponseEntity<List<LeavePolicyDto>> getAllLeavePolicies() {
        List<LeavePolicyDto> policies = leavePolicyService.getAllLeavePolicies();
        return ResponseEntity.ok(policies);
    }

    /**
     * GET /api/setup/wfm/leave-policies/{id} : Get the "id" leave policy.
     *
     * @param id the id of the leave policy to retrieve.
     * @return the ResponseEntity with status 200 (OK) and with body the leavePolicyDto, or with status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicyDto> getLeavePolicyById(@PathVariable Long id) {
        return leavePolicyService.getLeavePolicyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/setup/wfm/leave-policies/{id} : Updates an existing leave policy.
     *
     * @param id the id of the leave policy to update.
     * @param leavePolicyDto the leave policy to update.
     * @return the ResponseEntity with status 200 (OK) and with body the updated leavePolicyDto.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeavePolicyDto> updateLeavePolicy(@PathVariable Long id, @Valid @RequestBody LeavePolicyDto leavePolicyDto) {
        LeavePolicyDto updatedPolicy = leavePolicyService.updateLeavePolicy(id, leavePolicyDto);
        return ResponseEntity.ok(updatedPolicy);
    }

    /**
     * DELETE /api/setup/wfm/leave-policies/{id} : Delete the "id" leave policy.
     *
     * @param id the id of the leave policy to delete.
     * @return the ResponseEntity with status 204 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeavePolicy(@PathVariable Long id) {
        leavePolicyService.deleteLeavePolicy(id);
        return ResponseEntity.noContent().build();
    }
}