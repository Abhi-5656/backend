package com.wfm.experts.setup.wfm.requesttype.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "request_type_profile")
@Getter
@Setter
public class RequestTypeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_name", nullable = false, unique = true)
    private String profileName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "request_type_profile_request_types",
            joinColumns = @JoinColumn(name = "request_type_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "request_type_id")
    )
    private Set<RequestType> requestTypes = new HashSet<>();

}
