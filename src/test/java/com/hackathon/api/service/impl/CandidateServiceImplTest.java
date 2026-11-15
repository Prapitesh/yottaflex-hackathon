package com.hackathon.api.service.impl;

import com.hackathon.api.dto.request.candidate.CandidateRequest;
import com.hackathon.api.entity.AuditEvent;
import com.hackathon.api.entity.Candidate;
import com.hackathon.api.exception.DuplicateResourceException;
import com.hackathon.api.repository.AuditEventRepository;
import com.hackathon.api.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CandidateServiceImplTest {

    @Mock private CandidateRepository candidateRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private CandidateServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CandidateServiceImpl(candidateRepository, auditEventRepository);
    }

    @Test
    void createCandidateNormalizesEmailAndRecordsAudit() {
        when(candidateRepository.existsByEmailIgnoreCase("person@example.test"))
                .thenReturn(false);
        when(candidateRepository.save(any(Candidate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createCandidate(new CandidateRequest(
                "  Asha Rao  ", " PERSON@Example.Test ", "+91-9000000000",
                " Hyderabad ", 18));

        assertEquals("Asha Rao", response.fullName());
        assertEquals("person@example.test", response.email());
        assertEquals("Hyderabad", response.currentLocation());
        verify(candidateRepository).save(any(Candidate.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    void createCandidateRejectsDuplicateEmail() {
        when(candidateRepository.existsByEmailIgnoreCase("person@example.test"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.createCandidate(new CandidateRequest(
                        "Asha Rao", "person@example.test", null, null, 0)));

        verify(candidateRepository, never()).save(any(Candidate.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }
}
