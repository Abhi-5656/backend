package com.wfm.experts.modules.wfm.employee.leave.entity;

import com.wfm.experts.tenant.common.employees.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_request_approvals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leave_request_id")
    private LeaveRequest leaveRequest;

    @ManyToOne
    @JoinColumn(name = "approver_id", referencedColumnName = "employee_id")
    private Employee approver;

    @Column(name = "approval_level")
    private int approvalLevel;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED
}