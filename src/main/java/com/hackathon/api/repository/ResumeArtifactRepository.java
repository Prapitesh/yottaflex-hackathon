package com.hackathon.api.repository;

import com.hackathon.api.entity.ResumeArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for resume metadata; file bytes remain in external storage.
 */
public interface ResumeArtifactRepository extends JpaRepository<ResumeArtifact, String> {

    List<ResumeArtifact> findByCandidate_IdOrderByUploadedAtDesc(String candidateId);

    List<ResumeArtifact> findByChecksumSha256(String checksumSha256);
}
