package com.wfm.experts.modules.wfm.employee.leave.repository;

import com.wfm.experts.modules.wfm.employee.leave.entity.LeaveRequest;
import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<RequestType> findByLeavePolicyId(Long leavePolicyId);
}