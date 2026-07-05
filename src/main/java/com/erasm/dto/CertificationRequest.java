package com.erasm.dto;

import jakarta.validation.constraints.NotBlank;

public class CertificationRequest {

    @NotBlank(message = "Certification name is mandatory")
    private String certificationName;

    private String issuedBy;
    private String issuedDate;

    public CertificationRequest() {
    }

    public String getCertificationName() {
        return certificationName;
    }

    public void setCertificationName(String certificationName) {
        this.certificationName = certificationName;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }
}
