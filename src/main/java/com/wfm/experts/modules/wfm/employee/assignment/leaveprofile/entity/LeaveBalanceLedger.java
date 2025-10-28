package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.enums.LeaveTransactionType;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leave_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_policy_id")
    private LeavePolicy leavePolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "leave_transaction_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LeaveTransactionType transactionType;

    @Column(nullable = false)
    private double amount; // Positive for grants, negative for usage

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_request_id")
    private LeaveRequest relatedRequest;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
    }
}