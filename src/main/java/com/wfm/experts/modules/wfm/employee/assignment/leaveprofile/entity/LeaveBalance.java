package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment; // <-- IMPORT THIS
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "current_balance")
    private double currentBalance;

    @Column(name = "total_granted")
    private double totalGranted;

    @Column(name = "used_balance")
    private double usedBalance;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    @Column(name = "next_accrual_date")
    private LocalDate nextAccrualDate;

    @Column(name = "status")
    private String status;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // --- ADD THIS FIELD ---
    /**
     * Links this balance summary to the specific profile assignment that created it.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private LeaveProfileAssignment assignment;
    // --- END OF ADDITION ---

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