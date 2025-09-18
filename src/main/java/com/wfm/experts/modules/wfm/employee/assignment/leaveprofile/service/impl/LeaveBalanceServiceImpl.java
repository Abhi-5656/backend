package com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.impl;

import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.dto.LeaveBalanceDTO;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.entity.LeaveBalance;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.mapper.LeaveBalanceMapper;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.repository.LeaveBalanceRepository;
import com.wfm.experts.modules.wfm.employee.assignment.leaveprofile.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveBalanceMapper leaveBalanceMapper;

    @Override
    public List<LeaveBalanceDTO> getLeaveBalances(String employeeId) {
        List<LeaveBalance> leaveBalances = leaveBalanceRepository.findByEmployee_EmployeeId(employeeId);
        return leaveBalances.stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }
}