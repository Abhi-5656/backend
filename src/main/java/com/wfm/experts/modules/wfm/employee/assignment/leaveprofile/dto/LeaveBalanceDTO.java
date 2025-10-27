// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/dto/LeaveBalanceDTO.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto;

import com.fasterxml.jackson.annotation.JsonFormat; // <-- IMPORT
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime; // <-- IMPORT

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceDTO {
    private String leavePolicyName;
    private double currentBalance;
    private double totalGranted;
    private double usedBalance;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private LocalDate lastAccrualDate;
    private LocalDate nextAccrualDate;
    private String status;

    // --- NEW TIMESTAMP FIELDS ---
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}