package com.erasm.dto;

import java.util.List;

public class EmployeeResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String roleName;
    private String department;
    private String designation;
    private String dateOfJoining;
    private List<EmployeeSkillResponse> skills;
    private List<CertificationResponse> certifications;

    public EmployeeResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(String dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public List<EmployeeSkillResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<EmployeeSkillResponse> skills) {
        this.skills = skills;
    }

    public List<CertificationResponse> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<CertificationResponse> certifications) {
        this.certifications = certifications;
    }
}
