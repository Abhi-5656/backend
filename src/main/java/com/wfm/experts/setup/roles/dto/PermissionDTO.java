package com.wfm.experts.setup.roles.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {
    private Long id;
    private String permissionName;
    private String description;
}
