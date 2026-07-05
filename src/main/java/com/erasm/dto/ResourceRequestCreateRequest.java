package com.erasm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ResourceRequestCreateRequest {

    @NotNull(message = "Project id is mandatory")
    private Long projectId;

    @NotNull(message = "Skill id is mandatory")
    private Long skillId;

    @NotNull(message = "Required count is mandatory")
    @Min(value = 1, message = "Required count must be at least 1")
    private Integer requiredCount;

    public ResourceRequestCreateRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public Integer getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(Integer requiredCount) {
        this.requiredCount = requiredCount;
    }
}
