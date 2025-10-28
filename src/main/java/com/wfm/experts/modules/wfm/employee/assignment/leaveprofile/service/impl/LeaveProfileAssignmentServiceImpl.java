//package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;
//
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveProfileAssignmentMapper;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
//import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
//import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
//import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
//import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
//import com.wfm.experts.tenant.common.employees.entity.Employee;
//import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class LeaveProfileAssignmentServiceImpl implements LeaveProfileAssignmentService {
//
//    private final LeaveProfileAssignmentRepository assignmentRepository;
//    private final EmployeeRepository employeeRepository;
//    private final LeaveProfileRepository leaveProfileRepository;
//    private final LeaveProfileAssignmentMapper mapper;
//    private final LeaveAccrualService leaveAccrualService;
//
//    @Override
//    public List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto) {
//        leaveProfileRepository.findById(dto.getLeaveProfileId())
//                .orElseThrow(() -> new RuntimeException("LeaveProfile not found with id: " + dto.getLeaveProfileId()));
//
//        List<LeaveProfileAssignment> savedAssignments = new ArrayList<>();
//        for (String employeeId : dto.getEmployeeIds()) {
//            employeeRepository.findByEmployeeId(employeeId)
//                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
//
//            // Prevent re-assignment with the same effective date.
//            boolean exists = assignmentRepository.findByEmployeeId(employeeId).stream()
//                    .anyMatch(a -> a.isActive() && a.getEffectiveDate().equals(dto.getEffectiveDate()));
//            if (exists) {
//                throw new IllegalStateException("An active assignment with the same effective date already exists for employee: " + employeeId);
//            }
//
//            // Deactivate all existing assignments for this employee.
//            List<LeaveProfileAssignment> existingAssignments = assignmentRepository.findByEmployeeId(employeeId);
//            for (LeaveProfileAssignment existing : existingAssignments) {
//                existing.setActive(false);
//            }
//            assignmentRepository.saveAll(existingAssignments);
//
//            // Create the new, active assignment.
//            LeaveProfileAssignment newAssignment = LeaveProfileAssignment.builder()
//                    .employeeId(employeeId)
//                    .leaveProfileId(dto.getLeaveProfileId())
//                    .effectiveDate(dto.getEffectiveDate())
//                    .expirationDate(dto.getExpirationDate())
//                    .assignedAt(LocalDateTime.now())
//                    .active(true)
//                    .build();
//
//            savedAssignments.add(assignmentRepository.save(newAssignment));
//
//            // Trigger a full recalculation, which will now find the single active assignment.
//            leaveAccrualService.recalculateTotalLeaveBalance(employeeId);
//        }
//
//        return savedAssignments.stream()
//                .map(mapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
//        return assignmentRepository.findByEmployeeId(employeeId).stream()
//                .map(mapper::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<LeaveProfileAssignmentDTO> getAllAssignments() {
//        return assignmentRepository.findAll().stream()
//                .map(mapper::toDto)
//                .collect(Collectors.toList());
//    }
//}

package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveProfileAssignmentDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance; // Summary entity
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveProfileAssignment;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveProfileAssignmentMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository; // Summary repo
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveProfileAssignmentRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveProfileAssignmentService;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeavePolicy; // Import needed
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfile;
import com.wfm.experts.setup.wfm.leavepolicy.entity.LeaveProfilePolicy; // Import needed
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeavePolicyRepository; // Import needed
import com.wfm.experts.setup.wfm.leavepolicy.repository.LeaveProfileRepository;
import com.wfm.experts.setup.wfm.leavepolicy.service.LeaveAccrualService;
import com.wfm.experts.tenant.common.employees.entity.Employee;
import com.wfm.experts.tenant.common.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set; // Import Set
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveProfileAssignmentServiceImpl implements LeaveProfileAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveProfileAssignmentServiceImpl.class);

    private final LeaveProfileAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveProfileRepository leaveProfileRepository;
    private final LeaveBalanceRepository leaveBalanceRepository; // Summary repo
    private final LeaveProfileAssignmentMapper mapper;
    private final LeaveAccrualService leaveAccrualService;

    @Override
    public List<LeaveProfileAssignmentDTO> assignLeaveProfile(LeaveProfileAssignmentDTO dto) {
        LeaveProfile leaveProfile = leaveProfileRepository.findById(dto.getLeaveProfileId())
                .orElseThrow(() -> new RuntimeException("LeaveProfile not found with id: " + dto.getLeaveProfileId()));

        Set<LeaveProfilePolicy> policiesInProfile = leaveProfile.getLeaveProfilePolicies();
        if (policiesInProfile.isEmpty()) {
            throw new IllegalStateException("Cannot assign an empty leave profile with no policies.");
        }

        List<LeaveProfileAssignment> savedAssignments = new ArrayList<>();
        for (String employeeId : dto.getEmployeeIds()) {
            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

            // Prevent re-assignment with the same effective date.
            boolean exists = assignmentRepository.findByEmployeeId(employeeId).stream()
                    .anyMatch(a -> a.isActive() && a.getEffectiveDate().equals(dto.getEffectiveDate()));
            if (exists) {
                logger.warn("Attempted to assign leave profile {} to employee {} with effective date {}, but an active assignment already exists for that date.", dto.getLeaveProfileId(), employeeId, dto.getEffectiveDate());
                throw new IllegalStateException("An active assignment with the same effective date already exists for employee: " + employeeId);
            }

            // 1. Deactivate all existing assignments for this employee.
            List<LeaveProfileAssignment> existingAssignments = assignmentRepository.findByEmployeeId(employeeId);
            for (LeaveProfileAssignment existing : existingAssignments) {
                if (existing.isActive()) {
                    logger.info("Deactivating existing leave profile assignment ID {} for employee {}.", existing.getId(), employeeId);
                    existing.setActive(false);
                }
            }
            assignmentRepository.saveAll(existingAssignments);

            // 2. Deactivate old balance summary records by setting status and expiration
            List<LeaveBalance> oldBalances = leaveBalanceRepository.findByEmployee_EmployeeId(employeeId);
            for(LeaveBalance oldBalance : oldBalances) {
                if ("ACTIVE".equalsIgnoreCase(oldBalance.getStatus()) || "MANUAL_OVERRIDE".equalsIgnoreCase(oldBalance.getStatus())) {
                    logger.info("Deactivating existing leave balance summary ID {} (Policy: {}) for employee {}.", oldBalance.getId(), oldBalance.getLeavePolicy().getId(), employeeId);
                    oldBalance.setStatus("INACTIVE");
                    oldBalance.setExpirationDate(dto.getEffectiveDate().minusDays(1)); // Expire the day before the new one starts
                }
            }
            leaveBalanceRepository.saveAll(oldBalances);

            // 3. Create the new, active assignment.
            LeaveProfileAssignment newAssignment = LeaveProfileAssignment.builder()
                    .employeeId(employeeId)
                    .leaveProfileId(dto.getLeaveProfileId())
                    .effectiveDate(dto.getEffectiveDate())
                    .expirationDate(dto.getExpirationDate())
                    .assignedAt(LocalDateTime.now())
                    .active(true)
                    .build();

            LeaveProfileAssignment savedAssignment = assignmentRepository.save(newAssignment);
            savedAssignments.add(savedAssignment);
            logger.info("Created new active leave profile assignment ID {} for employee {}.", savedAssignment.getId(), employeeId);


            // --- 4. Create new, empty balance summary rows for this assignment ---
            for (LeaveProfilePolicy profilePolicy : policiesInProfile) {
                LeavePolicy policy = profilePolicy.getLeavePolicy();
                LeaveBalance newBalanceMetadata = LeaveBalance.builder()
                        .employee(employee) // Use the fetched employee entity
                        .leavePolicy(policy)
                        .status("ACTIVE")
                        .effectiveDate(dto.getEffectiveDate())
                        .expirationDate(dto.getExpirationDate())
                        .assignment(savedAssignment) // Link to the new assignment
                        .currentBalance(0.0) // Initialize balances to zero
                        .totalGranted(0.0)
                        .usedBalance(0.0)
                        .build();
                leaveBalanceRepository.save(newBalanceMetadata);
                logger.info("Created initial zero-balance summary row for employee {} and policy {}.", employeeId, policy.getId());
            }
            // --- END ---

            // 5. Trigger a full recalculation. This will:
            //    - Find the new active assignment.
            //    - Populate the ledger based on rules from the effective date.
            //    - Update the summary rows we just created.
            logger.info("Triggering recalculation for employee {}.", employeeId);
            leaveAccrualService.recalculateTotalLeaveBalance(employeeId);
        }

        return savedAssignments.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveProfileAssignmentDTO> getAssignmentsByEmployeeId(String employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveProfileAssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}