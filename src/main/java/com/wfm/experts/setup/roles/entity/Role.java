//package com.wfm.experts.setup.roles.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.List;
//
//@Entity
//@Table(name = "roles")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Role {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @Column(name = "role_name", unique = true, nullable = false)
//    private String roleName;
//
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "role_permissions",
//            joinColumns = @JoinColumn(name = "role_id"),
//            inverseJoinColumns = @JoinColumn(name = "permission_id")
//    )
//    private List<Permission> permissions;
//}