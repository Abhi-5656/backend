/*
 *
 *  * © 2024-2025 WFM EXPERTS INDIA PVT LTD. All rights reserved.
 *  *
 *  * This software, including all associated files, documentation, and related materials,
 *  * is the proprietary property of WFM EXPERTS INDIA PVT LTD. Unauthorized copying,
 *  * distribution, modification, or any form of use beyond the granted permissions
 *  * without prior written consent is strictly prohibited.
 *  *
 *  * DISCLAIMER:
 *  * This software is provided "as is," without warranty of any kind, express or implied,
 *  * including but not limited to the warranties of merchantability, fitness for a particular
 *  * purpose, and non-infringement.
 *  *
 *  * For inquiries, contact legal@wfmexperts.com.
 *
 */

package com.wfm.experts.security;

import com.wfm.experts.entity.tenant.common.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Employee employee;

    public CustomUserDetails(Employee employee) {
        this.employee = employee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming Role is a part of Employee and implements GrantedAuthority
        return Collections.singleton(new SimpleGrantedAuthority(employee.getRole().getRoleName()));
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Custom getter to access the Employee object
    public Employee getEmployee() {
        return this.employee;
    }
}
