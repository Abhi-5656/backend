// harshwfm/wfm-backend/HarshWfm-wfm-backend-573b561b9a0299c8388f2f15252dbc2875a7884a/src/main/java/com/wfm/experts/modules/wfm/employee/assignment/leaveprofile/entity/LeaveBalance.java
package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity;

import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Import LocalDate

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
//    @JoinColumn(name = "employee_id")
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "leave_policy_id")
    private LeavePolicy leavePolicy;

    private double balance;

    @Column(name = "effective_date", nullable = false) // Add effective_date field
    private LocalDate effectiveDate;

    @Column(name = "expiration_date") // Add expiration_date field (nullable)
    private LocalDate expirationDate;

    /**
     * NEW FIELD
     * Tracks the last date (e.g., end of month) that an automated accrual was
     * successfully processed for this balance.
     */
    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;
}