package com.erasm.dto;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String roleName;
    private boolean enabled;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String roleName, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roleName = roleName;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
