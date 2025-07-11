//package com.wfm.experts.setup.roles.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "permissions")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Permission {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @Column(name = "permission_name", unique = true, nullable = false)
//    private String permissionName;
//
//    @Column(name = "description")
//    private String description;
//}