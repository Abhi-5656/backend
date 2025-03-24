/*
 *
 *  * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 *  *
 *  * This software, including all associated files, documentation, and related materials,
 *  * is the proprietary property of WFM EXPERTS INDIA PVT LTD. Unauthorized copying,
 *  * distribution, modification, or any form of use beyond the granted permissions
 *  * without prior written consent is strictly prohibited.
 *  *
 *  * DISCLAIMER:
 *  * This software is provided "as is," without warranty of any kind, express or implied,
 *  * including but not limited to the warranties of merchantability, fitness for a particular
 *  * purpose, and non-infringement.
 *  *
 *  * For inquiries, contact legal@wfmexperts.com.
 *
 */

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
