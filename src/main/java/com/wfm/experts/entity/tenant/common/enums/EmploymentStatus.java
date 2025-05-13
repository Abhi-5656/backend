package com.wfm.experts.entity.tenant.common.enums;

public enum EmploymentStatus {
    ACTIVE,         // Actively employed and working
    PROBATION,      // Actively employed, but under a probationary period
    NOTICE_PERIOD,  // Employee has resigned or is being terminated, and is serving a notice period
    TERMINATED,     // Employment has been ended by the employer
    RESIGNED,       // Employee has voluntarily left the organization
    ON_LEAVE,       // Employee is on an approved leave (e.g., sabbatical, long-term medical leave)
    SUSPENDED,      // Employee is temporarily not allowed to work, pending investigation or disciplinary action
    ABSCONDING,     // Employee has abandoned their job without notice
    DECEASED
}