package com.erasm.service;

import com.erasm.dto.CertificationRequest;
import com.erasm.dto.CertificationResponse;
import com.erasm.entity.Certification;
import com.erasm.entity.Employee;
import com.erasm.exception.CertificationNotFoundException;
import com.erasm.exception.EmployeeNotFoundException;
import com.erasm.repository.CertificationRepository;
import com.erasm.repository.EmployeeRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Standalone implementation of {@link CertificationService}, exposed
 * independently via {@link com.erasm.controller.CertificationController}
 * in addition to the certification endpoints nested under
 * /api/employees/{employeeId}/certifications.
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationServiceImpl.class);

    private final CertificationRepository certificationRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public CertificationServiceImpl(CertificationRepository certificationRepository,
                                     EmployeeRepository employeeRepository,
                                     AuditService auditService) {
        this.certificationRepository = certificationRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    @Override
    public CertificationResponse addCertification(Long employeeId, CertificationRequest request) {
        Employee employee = findEmployeeOrThrow(employeeId);

        Certification certification = new Certification();
        certification.setEmployee(employee);
        certification.setCertificationName(request.getCertificationName());
        certification.setIssuedBy(request.getIssuedBy());
        certification.setIssuedDate(request.getIssuedDate());

        Certification saved = certificationRepository.save(certification);
        logger.info("Certification '{}' added for employee id {}", saved.getCertificationName(), employeeId);
        auditService.log("Certification", saved.getId(), "CREATE",
                "Certification " + saved.getCertificationName() + " added", SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    @Override
    public CertificationResponse updateCertification(Long employeeId, Long certificationId, CertificationRequest request) {
        Certification certification = findCertificationOrThrow(certificationId);

        if (!certification.getEmployee().getId().equals(employeeId)) {
            logger.warn("Attempt to update certification id {} that does not belong to employee id {}",
                    certificationId, employeeId);
            throw new IllegalArgumentException("This certification does not belong to the given employee");
        }

        if (request.getCertificationName() != null) {
            certification.setCertificationName(request.getCertificationName());
        }
        if (request.getIssuedBy() != null) {
            certification.setIssuedBy(request.getIssuedBy());
        }
        if (request.getIssuedDate() != null) {
            certification.setIssuedDate(request.getIssuedDate());
        }

        Certification saved = certificationRepository.save(certification);
        logger.info("Certification id {} updated for employee id {}", certificationId, employeeId);
        auditService.log("Certification", saved.getId(), "UPDATE", "Certification updated",
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    @Override
    public void deleteCertification(Long employeeId, Long certificationId) {
        Certification certification = findCertificationOrThrow(certificationId);

        if (!certification.getEmployee().getId().equals(employeeId)) {
            logger.warn("Attempt to delete certification id {} that does not belong to employee id {}",
                    certificationId, employeeId);
            throw new IllegalArgumentException("This certification does not belong to the given employee");
        }

        certificationRepository.delete(certification);
        logger.info("Certification id {} deleted for employee id {}", certificationId, employeeId);
        auditService.log("Certification", certificationId, "DELETE", "Certification deleted",
                SecurityUtil.getCurrentUserEmail());
    }

    @Override
    public List<CertificationResponse> getCertificationsByEmployee(Long employeeId) {
        findEmployeeOrThrow(employeeId);
        return certificationRepository.findByEmployee_Id(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    public CertificationResponse getCertificationById(Long certificationId) {
        return toResponse(findCertificationOrThrow(certificationId));
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
    }

    private Certification findCertificationOrThrow(Long certificationId) {
        return certificationRepository.findById(certificationId)
                .orElseThrow(() -> new CertificationNotFoundException(
                        "Certification not found with id: " + certificationId));
    }

    private CertificationResponse toResponse(Certification c) {
        CertificationResponse response = new CertificationResponse();
        response.setId(c.getId());
        response.setEmployeeId(c.getEmployee().getId());
        response.setCertificationName(c.getCertificationName());
        response.setIssuedBy(c.getIssuedBy());
        response.setIssuedDate(c.getIssuedDate());
        return response;
    }
}
