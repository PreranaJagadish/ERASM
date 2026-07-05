package com.erasm.service;

import com.erasm.dto.SkillRequest;
import com.erasm.dto.SkillResponse;
import com.erasm.entity.Skill;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.exception.SkillNotFoundException;
import com.erasm.repository.SkillRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private static final Logger logger = LoggerFactory.getLogger(SkillService.class);

    private final SkillRepository skillRepository;
    private final AuditService auditService;

    public SkillService(SkillRepository skillRepository, AuditService auditService) {
        this.skillRepository = skillRepository;
        this.auditService = auditService;
    }

    public SkillResponse addSkill(SkillRequest request) {
        if (skillRepository.existsBySkillNameIgnoreCase(request.getSkillName())) {
            throw new DuplicateResourceException("Skill '" + request.getSkillName() + "' already exists");
        }

        Skill skill = new Skill();
        skill.setSkillName(request.getSkillName());
        skill.setDescription(request.getDescription());
        Skill saved = skillRepository.save(skill);

        logger.info("Skill added: {}", saved.getSkillName());
        auditService.log("Skill", saved.getId(), "CREATE", "Skill created: " + saved.getSkillName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill skill = findSkillOrThrow(id);

        if (request.getSkillName() != null && !request.getSkillName().equalsIgnoreCase(skill.getSkillName())
                && skillRepository.existsBySkillNameIgnoreCase(request.getSkillName())) {
            throw new DuplicateResourceException("Skill '" + request.getSkillName() + "' already exists");
        }

        if (request.getSkillName() != null) {
            skill.setSkillName(request.getSkillName());
        }
        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }
        Skill saved = skillRepository.save(skill);

        logger.info("Skill updated: {}", saved.getSkillName());
        auditService.log("Skill", saved.getId(), "UPDATE", "Skill updated: " + saved.getSkillName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    public void deleteSkill(Long id) {
        Skill skill = findSkillOrThrow(id);
        skillRepository.delete(skill);
        logger.info("Skill deleted: {}", skill.getSkillName());
        auditService.log("Skill", id, "DELETE", "Skill deleted: " + skill.getSkillName(),
                SecurityUtil.getCurrentUserEmail());
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SkillResponse getSkillById(Long id) {
        return toResponse(findSkillOrThrow(id));
    }

    private Skill findSkillOrThrow(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + id));
    }

    private SkillResponse toResponse(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getSkillName(), skill.getDescription());
    }
}
