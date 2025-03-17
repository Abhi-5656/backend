package com.wfm.experts.controller;

import com.wfm.experts.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * API to create a new tenant schema.
     * @param tenantId Name of the tenant schema.
     * @return Success message.
     */
    @PostMapping("/{tenantId}")
    public ResponseEntity<String> createTenant(@PathVariable String tenantId) {
        try {
            tenantService.createTenantSchema(tenantId);
            return ResponseEntity.ok("Tenant schema '" + tenantId + "' created successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating schema: " + e.getMessage());
        }
    }
}
