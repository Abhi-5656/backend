package com.wfm.experts.setup.wfm.requesttype.repository;

import com.wfm.experts.setup.wfm.requesttype.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long>, JpaSpecificationExecutor<RequestType> {
    Optional<RequestType> findByLeavePolicyId(Long leavePolicyId);
}