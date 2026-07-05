package com.erasm.service;

import com.erasm.dto.SkillRequest;
import com.erasm.entity.Skill;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.exception.SkillNotFoundException;
import com.erasm.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private SkillService skillService;

    @Test
    void addSkill_withUniqueName_succeeds() {
        SkillRequest request = new SkillRequest();
        request.setSkillName("Java");
        request.setDescription("Core Java and Java EE");

        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(1L);
            return skill;
        });

        var response = skillService.addSkill(request);

        assertEquals("Java", response.getSkillName());
        assertEquals(1L, response.getId());
    }

    @Test
    void addSkill_withDuplicateName_throwsException() {
        SkillRequest request = new SkillRequest();
        request.setSkillName("Java");

        when(skillRepository.existsBySkillNameIgnoreCase("Java")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> skillService.addSkill(request));
    }

    @Test
    void getSkillById_notFound_throwsException() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SkillNotFoundException.class, () -> skillService.getSkillById(99L));
    }
}
