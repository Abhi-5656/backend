package com.wfm.experts.hr.recruitmentonboarding.repository;

import com.wfm.experts.hr.recruitmentonboarding.entity.Job;
import com.wfm.experts.hr.recruitmentonboarding.enums.ExperienceLevel;
import com.wfm.experts.hr.recruitmentonboarding.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // 🔍 Optional: Custom query methods if needed

    List<Job> findByStatus(JobStatus status);

    List<Job> findByTitleContainingIgnoreCase(String keyword);

    List<Job> findByExperienceLevel(ExperienceLevel experienceLevel);
}
