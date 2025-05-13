package com.wfm.experts.validation.groups;


import jakarta.validation.groups.Default;

/**
 * Validation group for constraints that should be checked only during
 * the initial creation of an Admin User (e.g., during subscription).
 * This group can extend Default if you want Default group constraints to also apply.
 */
public interface OnAdminCreation extends Default {
}