package com.wfm.experts.setup.orgstructure.repository;

import com.wfm.experts.setup.orgstructure.entity.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    boolean existsByCodeIgnoreCase(String code);
}
