package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lp_eligibility_config")
@Getter
@Setter
public class EligibilityConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}