package com.wfm.experts.entity.tenant.common;

import org.springframework.security.core.GrantedAuthority;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority { // ✅ Implement GrantedAuthority

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roleName;

    @Override
    public String getAuthority() {
        return roleName; // ✅ Required for Spring Security
    }
}
