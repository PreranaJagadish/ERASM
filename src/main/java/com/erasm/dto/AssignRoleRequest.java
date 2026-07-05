package com.erasm.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignRoleRequest {

    @NotBlank(message = "Role is mandatory")
    private String roleName;

    public AssignRoleRequest() {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
