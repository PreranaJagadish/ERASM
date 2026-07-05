package com.erasm.dto;

import java.util.List;

public class SkillReportResponse {

    private String skillName;
    private List<String> employeeNames;
    private int employeeCount;

    public SkillReportResponse() {
    }

    public SkillReportResponse(String skillName, List<String> employeeNames) {
        this.skillName = skillName;
        this.employeeNames = employeeNames;
        this.employeeCount = employeeNames == null ? 0 : employeeNames.size();
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public List<String> getEmployeeNames() {
        return employeeNames;
    }

    public void setEmployeeNames(List<String> employeeNames) {
        this.employeeNames = employeeNames;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
}
