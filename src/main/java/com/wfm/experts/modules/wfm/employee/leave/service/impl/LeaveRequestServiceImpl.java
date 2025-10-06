////package com.wfm.experts.modules.wfm.employee.leave.service.impl;
////
////import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
////import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
////import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
////import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
////import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
////import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
////import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
////import com.wfm.experts.modules.wfm.employee.leave.enums.LeaveStatus;
////import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestApprovalMapper;
////import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestMapper;
////import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestApprovalRepository;
////import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestRepository;
////import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
////import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
////import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
////import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
////import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
////import com.wfm.experts.tenant.common.employees.entity.Employee;
////import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
////import jakarta.transaction.Transactional;
////import lombok.RequiredArgsConstructor;
////import org.springframework.stereotype.Service;
////
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////@RequiredArgsConstructor
////@Transactional
////public class LeaveRequestServiceImpl implements LeaveRequestService {
////
////    private final LeaveRequestRepository leaveRequestRepository;
////    private final LeaveBalanceRepository leaveBalanceRepository;
////    private final EmployeeRepository employeeRepository;
////    private final RequestTypeRepository requestTypeRepository;
////    private final LeavePolicyRepository leavePolicyRepository;
////    private final LeaveRequestMapper leaveRequestMapper;
////    private final LeaveRequestApprovalRepository leaveRequestApprovalRepository;
////    private final LeaveRequestApprovalMapper leaveRequestApprovalMapper;
////
////
////    @Override
////    public LeaveRequestDTO applyForLeave(LeaveRequestDTO leaveRequestDTO) {
////        Employee employee = employeeRepository.findByEmployeeId(leaveRequestDTO.getEmployeeId())
////                .orElseThrow(() -> new RuntimeException("Employee not found"));
////
////        LeavePolicy leavePolicy = leavePolicyRepository.findById(leaveRequestDTO.getLeavePolicyId())
////                .orElseThrow(() -> new RuntimeException("Leave Policy not found"));
////
////        RequestType requestType = requestTypeRepository.findByLeavePolicyId(leavePolicy.getId())
////                .orElseThrow(() -> new RuntimeException("No Request Type is configured for this Leave Policy."));
////
////
////        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
////                .orElseThrow(() -> new RuntimeException("Leave balance not found for this policy"));
////
////
////        if (leaveBalance.getBalance() < leaveRequestDTO.getLeaveDays()) {
////            throw new RuntimeException("Insufficient leave balance");
////        }
////
////        // Deduct balance immediately upon application
////        leaveBalance.setBalance(leaveBalance.getBalance() - leaveRequestDTO.getLeaveDays());
////        leaveBalanceRepository.save(leaveBalance);
////
////        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(leaveRequestDTO);
////        leaveRequest.setEmployee(employee);
////        leaveRequest.setLeavePolicy(leavePolicy);
////
////        if (requestType.getApproval() != null && requestType.getApproval().isEnabled()) {
////            leaveRequest.setStatus(LeaveStatus.PENDING_APPROVAL);
////            LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
////            createApprovalChain(savedLeaveRequest, requestType.getApproval().getChainSteps());
////        } else {
////            leaveRequest.setStatus(LeaveStatus.APPROVED);
////        }
////
////        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
////        return leaveRequestMapper.toDto(savedLeaveRequest);
////    }
////
////    private void createApprovalChain(LeaveRequest leaveRequest, List<String> chainSteps) {
////        Employee applicant = leaveRequest.getEmployee();
////
////        for (int i = 0; i < chainSteps.size(); i++) {
////            String step = chainSteps.get(i);
////            Employee approver;
////
////            if ("manager".equalsIgnoreCase(step)) {
////                approver = applicant.getReportingManager();
////                if (approver == null) {
////                    throw new RuntimeException("Reporting manager is not assigned for employee: " + applicant.getEmployeeId());
////                }
////            } else if ("hr".equalsIgnoreCase(step)) {
////                approver = applicant.getHrManager();
////                if (approver == null) {
////                    throw new RuntimeException("HR manager is not assigned for employee: " + applicant.getEmployeeId());
////                }
////            } else {
////                // Treat the step as a role name and find the first employee with that role
////                approver = employeeRepository.findFirstByRoles_RoleName(step)
////                        .orElseThrow(() -> new RuntimeException("No employee found with the role: " + step));
////            }
////
////            String initialStatus = (i == 0) ? "PENDING" : "WAITING";
////
////            LeaveRequestApproval approval = LeaveRequestApproval.builder()
////                    .leaveRequest(leaveRequest)
////                    .approver(approver)
////                    .approvalLevel(i + 1)
////                    .status(initialStatus)
////                    .build();
////            leaveRequestApprovalRepository.save(approval);
////        }
////    }
////
////    @Override
////    public List<LeaveRequestApprovalDTO> getPendingApprovals(String approverId) {
////        return leaveRequestApprovalRepository.findByApprover_EmployeeIdAndStatus(approverId, "PENDING")
////                .stream()
////                .map(leaveRequestApprovalMapper::toDto)
////                .collect(Collectors.toList());
////    }
////
////    @Override
////    public LeaveRequestActionResponseDTO approveOrRejectLeave(Long approvalId, String approverId, boolean approved) {
////        LeaveRequestApproval approval = leaveRequestApprovalRepository.findById(approvalId)
////                .orElseThrow(() -> new RuntimeException("Approval not found"));
////
////        if (!approval.getApprover().getEmployeeId().equals(approverId)) {
////            throw new RuntimeException("You are not authorized to approve this request");
////        }
////
////        if (!"PENDING".equals(approval.getStatus())) {
////            throw new RuntimeException("This request is not currently pending your approval.");
////        }
////
////        approval.setStatus(approved ? "APPROVED" : "REJECTED");
////        leaveRequestApprovalRepository.save(approval);
////
////        LeaveRequest leaveRequest = approval.getLeaveRequest();
////        if (!approved) {
////            leaveRequest.setStatus(LeaveStatus.REJECTED);
////            leaveRequestRepository.save(leaveRequest);
////            restoreLeaveBalance(leaveRequest);
////            return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "REJECTED", "Leave request has been rejected.");
////
////        } else {
////            List<LeaveRequestApproval> nextApprovals = leaveRequestApprovalRepository.findAll().stream()
////                    .filter(a -> a.getLeaveRequest().getId().equals(leaveRequest.getId()) && a.getApprovalLevel() == approval.getApprovalLevel() + 1)
////                    .collect(Collectors.toList());
////
////            if (!nextApprovals.isEmpty()) {
////                for(LeaveRequestApproval nextApproval : nextApprovals) {
////                    nextApproval.setStatus("PENDING");
////                    leaveRequestApprovalRepository.save(nextApproval);
////                }
////                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "PENDING_APPROVAL", "Approved. Sent to next level.");
////            } else {
////                leaveRequest.setStatus(LeaveStatus.APPROVED);
////                leaveRequestRepository.save(leaveRequest);
////                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "APPROVED", "Leave request has been fully approved.");
////            }
////        }
////    }
////
////    @Override
////    public LeaveRequestActionResponseDTO cancelLeaveRequest(Long leaveRequestId, String employeeId) {
////        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
////                .orElseThrow(() -> new RuntimeException("Leave request not found"));
////
////        if (!leaveRequest.getEmployee().getEmployeeId().equals(employeeId)) {
////            throw new RuntimeException("You are not authorized to cancel this request.");
////        }
////
////        if (leaveRequest.getStatus() != LeaveStatus.PENDING_APPROVAL) {
////            throw new RuntimeException("Cannot cancel a leave request that is not pending approval.");
////        }
////
////        leaveRequest.setStatus(LeaveStatus.CANCELLED);
////        leaveRequestRepository.save(leaveRequest);
////        restoreLeaveBalance(leaveRequest);
////
////        return new LeaveRequestActionResponseDTO(leaveRequestId, "CANCELLED", "Leave request has been cancelled.");
////    }
////
////    private void restoreLeaveBalance(LeaveRequest leaveRequest) {
////        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(
////                        leaveRequest.getEmployee().getEmployeeId(), leaveRequest.getLeavePolicy().getId())
////                .orElseThrow(() -> new RuntimeException("Leave balance not found for restoration"));
////        leaveBalance.setBalance(leaveBalance.getBalance() + leaveRequest.getLeaveDays());
////        leaveBalanceRepository.save(leaveBalance);
////    }
////}
package com.wfm.experts.modules.wfm.employee.leave.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
import com.wfm.experts.modules.wfm.employee.leave.enums.LeaveStatus;
import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestApprovalMapper;
import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestMapper;
import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestApprovalRepository;
import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestRepository;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestNotificationService;
import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveRequestApprovalRepository leaveRequestApprovalRepository;
    private final LeaveRequestApprovalMapper leaveRequestApprovalMapper;
    private final LeaveRequestNotificationService notificationService;

    @Override
    public LeaveRequestDTO applyForLeave(LeaveRequestDTO leaveRequestDTO) {
        Employee employee = employeeRepository.findByEmployeeId(leaveRequestDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeavePolicy leavePolicy = leavePolicyRepository.findById(leaveRequestDTO.getLeavePolicyId())
                .orElseThrow(() -> new RuntimeException("Leave Policy not found"));

        RequestType requestType = requestTypeRepository.findByLeavePolicyId(leavePolicy.getId())
                .orElseThrow(() -> new RuntimeException("No Request Type is configured for this Leave Policy."));


        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found for this policy"));


        if (leaveBalance.getBalance() < leaveRequestDTO.getLeaveDays()) {
            throw new RuntimeException("Insufficient leave balance");
        }

        // Deduct balance immediately upon application
        leaveBalance.setBalance(leaveBalance.getBalance() - leaveRequestDTO.getLeaveDays());
        leaveBalanceRepository.save(leaveBalance);

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(leaveRequestDTO);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeavePolicy(leavePolicy);

        if (requestType.getApproval() != null && requestType.getApproval().isEnabled()) {
            leaveRequest.setStatus(LeaveStatus.PENDING_APPROVAL);
            LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
            createApprovalChain(savedLeaveRequest, requestType.getApproval().getChainSteps());
        } else {
            leaveRequest.setStatus(LeaveStatus.APPROVED);
        }

        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return leaveRequestMapper.toDto(savedLeaveRequest);
    }

    private void createApprovalChain(LeaveRequest leaveRequest, List<String> chainSteps) {
        Employee applicant = leaveRequest.getEmployee();

        for (int i = 0; i < chainSteps.size(); i++) {
            String step = chainSteps.get(i);
            Employee approver;

            if ("manager".equalsIgnoreCase(step)) {
                approver = applicant.getReportingManager();
                if (approver == null) {
                    throw new RuntimeException("Reporting manager is not assigned for employee: " + applicant.getEmployeeId());
                }
            } else if ("hr".equalsIgnoreCase(step)) {
                approver = applicant.getHrManager();
                if (approver == null) {
                    throw new RuntimeException("HR manager is not assigned for employee: " + applicant.getEmployeeId());
                }
            } else {
                // Treat the step as a role name and find the first employee with that role
                approver = employeeRepository.findFirstByRoles_RoleName(step)
                        .orElseThrow(() -> new RuntimeException("No employee found with the role: " + step));
            }

            String initialStatus = (i == 0) ? "PENDING" : "WAITING";

            LeaveRequestApproval approval = LeaveRequestApproval.builder()
                    .leaveRequest(leaveRequest)
                    .approver(approver)
                    .approvalLevel(i + 1)
                    .status(initialStatus)
                    .build();
            LeaveRequestApproval savedApproval = leaveRequestApprovalRepository.save(approval);
            if (i == 0) {
                notificationService.sendLeaveRequestSubmissionNotifications(leaveRequest, savedApproval);
            }
        }
    }

    @Override
    public List<LeaveRequestApprovalDTO> getPendingApprovals(String approverId) {
        return leaveRequestApprovalRepository.findByApprover_EmployeeIdAndStatus(approverId, "PENDING")
                .stream()
                .map(leaveRequestApprovalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestActionResponseDTO approveOrRejectLeave(Long approvalId, String approverId, boolean approved) {
        LeaveRequestApproval approval = leaveRequestApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        if (!approval.getApprover().getEmployeeId().equals(approverId)) {
            throw new RuntimeException("You are not authorized to approve this request");
        }

        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("This request is not currently pending your approval.");
        }

        approval.setStatus(approved ? "APPROVED" : "REJECTED");
        leaveRequestApprovalRepository.save(approval);

        LeaveRequest leaveRequest = approval.getLeaveRequest();
        if (!approved) {
            leaveRequest.setStatus(LeaveStatus.REJECTED);
            leaveRequestRepository.save(leaveRequest);
            restoreLeaveBalance(leaveRequest);
            notificationService.sendLeaveRequestRejectionNotifications(leaveRequest, approval.getApprover());
            return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "REJECTED", "Leave request has been rejected.");

        } else {
            List<LeaveRequestApproval> nextApprovals = leaveRequestApprovalRepository.findAll().stream()
                    .filter(a -> a.getLeaveRequest().getId().equals(leaveRequest.getId()) && a.getApprovalLevel() == approval.getApprovalLevel() + 1)
                    .collect(Collectors.toList());

            if (!nextApprovals.isEmpty()) {
                for(LeaveRequestApproval nextApproval : nextApprovals) {
                    nextApproval.setStatus("PENDING");
                    LeaveRequestApproval savedNextApproval = leaveRequestApprovalRepository.save(nextApproval);
                    notificationService.sendLeaveRequestApprovalNotifications(leaveRequest, approval.getApprover(), savedNextApproval);
                }
                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "PENDING_APPROVAL", "Approved. Sent to next level.");
            } else {
                leaveRequest.setStatus(LeaveStatus.APPROVED);
                leaveRequestRepository.save(leaveRequest);
                notificationService.sendLeaveRequestApprovalNotifications(leaveRequest, approval.getApprover(), null);
                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "APPROVED", "Leave request has been fully approved.");
            }
        }
    }

    @Override
    public LeaveRequestActionResponseDTO cancelLeaveRequest(Long leaveRequestId, String employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leaveRequest.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("You are not authorized to cancel this request.");
        }

        if (leaveRequest.getStatus() != LeaveStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Cannot cancel a leave request that is not pending approval.");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);
        restoreLeaveBalance(leaveRequest);
        notificationService.sendLeaveRequestCancellationNotifications(leaveRequest);

        return new LeaveRequestActionResponseDTO(leaveRequestId, "CANCELLED", "Leave request has been cancelled.");
    }

    private void restoreLeaveBalance(LeaveRequest leaveRequest) {
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(
                        leaveRequest.getEmployee().getEmployeeId(), leaveRequest.getLeavePolicy().getId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found for restoration"));
        leaveBalance.setBalance(leaveBalance.getBalance() + leaveRequest.getLeaveDays());
        leaveBalanceRepository.save(leaveBalance);
    }
}
//package com.wfm.experts.modules.wfm.employee.leave.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
//import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestActionResponseDTO;
//import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestApprovalDTO;
//import com.wfm.experts.modules.wfm.employee.leave.dto.LeaveRequestDTO;
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
//import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequestApproval;
//import com.wfm.experts.modules.wfm.employee.leave.enums.LeaveStatus;
//import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestApprovalMapper;
//import com.wfm.experts.modules.wfm.employee.leave.mapper.LeaveRequestMapper;
//import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestApprovalRepository;
//import com.wfm.experts.modules.wfm.employee.leave.repository.LeaveRequestRepository;
//import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestNotificationService;
//import com.wfm.experts.modules.wfm.employee.leave.service.LeaveRequestService;
//import com.wfm.experts.modules.wfm.features.timesheet.entity.Timesheet;
//import com.wfm.experts.modules.wfm.features.timesheet.repository.TimesheetRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy;
//import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository;
//import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
//import com.wfm.experts.setup.wfm.requesttype.repository.RequestTypeRepository;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class LeaveRequestServiceImpl implements LeaveRequestService {
//
//    private final LeaveRequestRepository leaveRequestRepository;
//    private final LeaveBalanceRepository leaveBalanceRepository;
//    private final EmployeeRepository employeeRepository;
//    private final RequestTypeRepository requestTypeRepository;
//    private final LeavePolicyRepository leavePolicyRepository;
//    private final LeaveRequestMapper leaveRequestMapper;
//    private final LeaveRequestApprovalRepository leaveRequestApprovalRepository;
//    private final LeaveRequestApprovalMapper leaveRequestApprovalMapper;
//    private final LeaveRequestNotificationService notificationService;
//    private final TimesheetRepository timesheetRepository;
//
//
//    @Override
//    public LeaveRequestDTO applyForLeave(LeaveRequestDTO leaveRequestDTO) {
//        Employee employee = employeeRepository.findByEmployeeId(leaveRequestDTO.getEmployeeId())
//                .orElseThrow(() -> new RuntimeException("Employee not found"));
//
//        LeavePolicy leavePolicy = leavePolicyRepository.findById(leaveRequestDTO.getLeavePolicyId())
//                .orElseThrow(() -> new RuntimeException("Leave Policy not found"));
//
//        RequestType requestType = requestTypeRepository.findByLeavePolicyId(leavePolicy.getId())
//                .orElseThrow(() -> new RuntimeException("No Request Type is configured for this Leave Policy."));
//
//
//        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(employee.getEmployeeId(), leavePolicy.getId())
//                .orElseThrow(() -> new RuntimeException("Leave balance not found for this policy"));
//
//
//        if (leaveBalance.getBalance() < leaveRequestDTO.getLeaveDays()) {
//            throw new RuntimeException("Insufficient leave balance");
//        }
//
//        // Deduct balance immediately upon application
//        leaveBalance.setBalance(leaveBalance.getBalance() - leaveRequestDTO.getLeaveDays());
//        leaveBalanceRepository.save(leaveBalance);
//
//        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(leaveRequestDTO);
//        leaveRequest.setEmployee(employee);
//        leaveRequest.setLeavePolicy(leavePolicy);
//
//        if (requestType.getApproval() != null && requestType.getApproval().isEnabled()) {
//            leaveRequest.setStatus(LeaveStatus.PENDING_APPROVAL);
//            LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
//            createApprovalChain(savedLeaveRequest, requestType.getApproval().getChainSteps());
//        } else {
//            leaveRequest.setStatus(LeaveStatus.APPROVED);
//        }
//
//        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
//        return leaveRequestMapper.toDto(savedLeaveRequest);
//    }
//
//    private void createApprovalChain(LeaveRequest leaveRequest, List<String> chainSteps) {
//        Employee applicant = leaveRequest.getEmployee();
//
//        for (int i = 0; i < chainSteps.size(); i++) {
//            String step = chainSteps.get(i);
//            Employee approver;
//
//            if ("manager".equalsIgnoreCase(step)) {
//                approver = applicant.getReportingManager();
//                if (approver == null) {
//                    throw new RuntimeException("Reporting manager is not assigned for employee: " + applicant.getEmployeeId());
//                }
//            } else if ("hr".equalsIgnoreCase(step)) {
//                approver = applicant.getHrManager();
//                if (approver == null) {
//                    throw new RuntimeException("HR manager is not assigned for employee: " + applicant.getEmployeeId());
//                }
//            } else {
//                // Treat the step as a role name and find the first employee with that role
//                approver = employeeRepository.findFirstByRoles_RoleName(step)
//                        .orElseThrow(() -> new RuntimeException("No employee found with the role: " + step));
//            }
//
//            String initialStatus = (i == 0) ? "PENDING" : "WAITING";
//
//            LeaveRequestApproval approval = LeaveRequestApproval.builder()
//                    .leaveRequest(leaveRequest)
//                    .approver(approver)
//                    .approvalLevel(i + 1)
//                    .status(initialStatus)
//                    .build();
//            LeaveRequestApproval savedApproval = leaveRequestApprovalRepository.save(approval);
//            if (i == 0) {
//                notificationService.sendLeaveRequestSubmissionNotifications(leaveRequest, savedApproval);
//            }
//        }
//    }
//
//    @Override
//    public List<LeaveRequestApprovalDTO> getPendingApprovals(String approverId) {
//        return leaveRequestApprovalRepository.findByApprover_EmployeeIdAndStatus(approverId, "PENDING")
//                .stream()
//                .map(leaveRequestApprovalMapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public LeaveRequestActionResponseDTO approveOrRejectLeave(Long approvalId, String approverId, boolean approved) {
//        LeaveRequestApproval approval = leaveRequestApprovalRepository.findById(approvalId)
//                .orElseThrow(() -> new RuntimeException("Approval not found"));
//
//        if (!approval.getApprover().getEmployeeId().equals(approverId)) {
//            throw new RuntimeException("You are not authorized to approve this request");
//        }
//
//        if (!"PENDING".equals(approval.getStatus())) {
//            throw new RuntimeException("This request is not currently pending your approval.");
//        }
//
//        approval.setStatus(approved ? "APPROVED" : "REJECTED");
//        leaveRequestApprovalRepository.save(approval);
//
//        LeaveRequest leaveRequest = approval.getLeaveRequest();
//        if (!approved) {
//            leaveRequest.setStatus(LeaveStatus.REJECTED);
//            leaveRequestRepository.save(leaveRequest);
//            restoreLeaveBalance(leaveRequest);
//            notificationService.sendLeaveRequestRejectionNotifications(leaveRequest, approval.getApprover());
//            return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "REJECTED", "Leave request has been rejected.");
//
//        } else {
//            List<LeaveRequestApproval> nextApprovals = leaveRequestApprovalRepository.findAll().stream()
//                    .filter(a -> a.getLeaveRequest().getId().equals(leaveRequest.getId()) && a.getApprovalLevel() == approval.getApprovalLevel() + 1)
//                    .collect(Collectors.toList());
//
//            if (!nextApprovals.isEmpty()) {
//                for(LeaveRequestApproval nextApproval : nextApprovals) {
//                    nextApproval.setStatus("PENDING");
//                    LeaveRequestApproval savedNextApproval = leaveRequestApprovalRepository.save(nextApproval);
//                    notificationService.sendLeaveRequestApprovalNotifications(leaveRequest, approval.getApprover(), savedNextApproval);
//                }
//                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "PENDING_APPROVAL", "Approved. Sent to next level.");
//            } else {
//                leaveRequest.setStatus(LeaveStatus.APPROVED);
//                leaveRequestRepository.save(leaveRequest);
//
//                // Create timesheet records for the leave period
//                LocalDate startDate = leaveRequest.getStartDate();
//                LocalDate endDate = leaveRequest.getEndDate();
//                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//                    Timesheet timesheet = timesheetRepository.findByEmployeeIdAndWorkDate(leaveRequest.getEmployee().getEmployeeId(), date)
//                            .orElse(new Timesheet());
//
//                    timesheet.setEmployeeId(leaveRequest.getEmployee().getEmployeeId());
//                    timesheet.setWorkDate(date);
//                    timesheet.setStatus(leaveRequest.getLeavePolicy().getPolicyName());
//                    timesheetRepository.save(timesheet);
//                }
//
//                notificationService.sendLeaveRequestApprovalNotifications(leaveRequest, approval.getApprover(), null);
//                return new LeaveRequestActionResponseDTO(leaveRequest.getId(), "APPROVED", "Leave request has been fully approved.");
//            }
//        }
//    }
//
//    @Override
//    public LeaveRequestActionResponseDTO cancelLeaveRequest(Long leaveRequestId, String employeeId) {
//        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
//                .orElseThrow(() -> new RuntimeException("Leave request not found"));
//
//        if (!leaveRequest.getEmployee().getEmployeeId().equals(employeeId)) {
//            throw new RuntimeException("You are not authorized to cancel this request.");
//        }
//
//        if (leaveRequest.getStatus() != LeaveStatus.PENDING_APPROVAL) {
//            throw new RuntimeException("Cannot cancel a leave request that is not pending approval.");
//        }
//
//        leaveRequest.setStatus(LeaveStatus.CANCELLED);
//        leaveRequestRepository.save(leaveRequest);
//        restoreLeaveBalance(leaveRequest);
//        notificationService.sendLeaveRequestCancellationNotifications(leaveRequest);
//
//        return new LeaveRequestActionResponseDTO(leaveRequestId, "CANCELLED", "Leave request has been cancelled.");
//    }
//
//    private void restoreLeaveBalance(LeaveRequest leaveRequest) {
//        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_EmployeeIdAndLeavePolicy_Id(
//                        leaveRequest.getEmployee().getEmployeeId(), leaveRequest.getLeavePolicy().getId())
//                .orElseThrow(() -> new RuntimeException("Leave balance not found for restoration"));
//        leaveBalance.setBalance(leaveBalance.getBalance() + leaveRequest.getLeaveDays());
//        leaveBalanceRepository.save(leaveBalance);
//    }
//}