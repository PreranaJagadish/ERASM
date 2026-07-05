package com.erasm.service;

import com.erasm.dto.CertificationRequest;
import com.erasm.dto.CertificationResponse;

import java.util.List;

/**
 * Dedicated service contract for managing Employee Certifications
 * (part of Module 3: Employee Skill Profile).
 */
public interface CertificationService {

    CertificationResponse addCertification(Long employeeId, CertificationRequest request);

    CertificationResponse updateCertification(Long employeeId, Long certificationId, CertificationRequest request);

    void deleteCertification(Long employeeId, Long certificationId);

    List<CertificationResponse> getCertificationsByEmployee(Long employeeId);

    CertificationResponse getCertificationById(Long certificationId);
}
