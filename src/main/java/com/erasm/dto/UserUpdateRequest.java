package com.erasm.dto;

import jakarta.validation.constraints.Email;

public class UserUpdateRequest {

    private String name;

    @Email(message = "Email must be a valid email address")
    private String email;

    public UserUpdateRequest() {
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
}
