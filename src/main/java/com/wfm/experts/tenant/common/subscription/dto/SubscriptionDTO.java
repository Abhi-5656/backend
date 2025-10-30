package com.wfm.experts.tenant.common.subscription.dto;

import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class SubscriptionDTO {

    private Long id;

    @NotBlank(message = "entityType is required")
    private String entityType;

    // === Required commercial/org fields ===
    @NotBlank(message = "companyName is required")
    private String companyName;

    // Optional but if present, validate format (still keep service-side GST check too)
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}Z[A-Z0-9]{1}$",
            message = "companyGstNumber must be a valid GST number"
    )
    private String companyGstNumber;

    @NotBlank(message = "subscriptionType is required")
    private String subscriptionType;     // e.g., Annual / Monthly

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "price must be > 0")
    private BigDecimal price;

    @NotBlank(message = "currency is required")
    private String currency;             // e.g., INR

    // Lifecycle (allow null; service sets defaults)
    private Date subscriptionStart;
    private Date subscriptionEnd;
    private Boolean isActive;
    private String paymentStatus;

    // System/status (service computes/sets these)
    private String status;
    private String tenantId;
    private String transactionId;
    private String tenantURL;

    private Date purchaseDate;
    private Date activationDate;
    private Instant createdAt;
    private Instant updatedAt;

    // Modules must be present and non-empty; each module validated
    @NotNull(message = "modules cannot be null")
    @Size(min = 1, message = "at least one module is required")
    private List<@Valid SubscriptionModuleDTO> modules;
}
