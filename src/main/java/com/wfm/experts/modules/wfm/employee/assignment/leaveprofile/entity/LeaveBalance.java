// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/entity/LeaveBalance.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime; // <-- IMPORT

@Entity
@Table(name = "employee_leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "leave_policy_id")
    private LeavePolicy leavePolicy;

    @Column(name = "current_balance") // Renamed
    private double currentBalance;

    @Column(name = "total_granted") // New
    private double totalGranted;

    @Column(name = "used_balance") // New
    private double usedBalance;

    @Column(name = "last_accrual_date") // New
    private LocalDate lastAccrualDate;

    @Column(name = "next_accrual_date") // New
    private LocalDate nextAccrualDate;

    @Column(name = "status") // New
    private String status;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // --- NEW TIMESTAMP FIELDS ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}