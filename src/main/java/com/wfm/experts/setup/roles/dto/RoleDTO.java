package com.wfm.experts.setup.roles.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private Long id;
    private String roleName;
    private List<String> permissionNames;
}
