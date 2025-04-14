package com.wfm.experts.hr.recruitmentonboarding.application.repository;

import com.wfm.experts.hr.recruitmentonboarding.application.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // 🔍 Find all applications for a particular job
    List<JobApplication> findByJobId(Long jobId);

    // 🔍 Search applications by email (e.g. for deduplication or lookup)
    JobApplication findByEmailAndJobId(String email, Long jobId);

    // Optional: find by phone
    JobApplication findByPhoneAndJobId(String phone, Long jobId);
}
