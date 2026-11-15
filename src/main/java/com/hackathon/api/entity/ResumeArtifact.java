package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Metadata for an uploaded resume.
 *
 * The actual file is stored outside the relational database. This entity
 * stores its location and integrity metadata. applicationId remains optional
 * and is mapped as a scalar until the Application entity is introduced.
 */
@Entity
@Table(name = "resume_artifact")
public class ResumeArtifact {

    @Id
    @NotBlank
    @Size(max = 40)
    @Column(name = "resume_id", nullable = false, length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Size(max = 80)
    @Column(name = "application_id", length = 80)
    private String applicationId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Column(name = "storage_uri", nullable = false, columnDefinition = "TEXT")
    private String storageUri;

    @NotBlank
    @Size(max = 100)
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @NotBlank
    @Pattern(regexp = "^[a-fA-F0-9]{64}$",
            message = "SHA-256 checksum must contain exactly 64 hexadecimal characters")
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "parsed_at")
    private OffsetDateTime parsedAt;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    /** Required by JPA. Use the public constructor in application code. */
    protected ResumeArtifact() {
    }

    public ResumeArtifact(
            String id,
            Candidate candidate,
            String applicationId,
            String fileName,
            String storageUri,
            String mimeType,
            String checksumSha256
    ) {
        this.id = id;
        this.candidate = candidate;
        this.applicationId = applicationId;
        this.fileName = fileName;
        this.storageUri = storageUri;
        this.mimeType = mimeType;
        this.checksumSha256 = checksumSha256;
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }

    /** Associate this resume with an application after the application exists. */
    public void linkToApplication(String applicationId) {
        this.applicationId = applicationId;
    }

    /** Record when resume parsing completed; null means not parsed yet. */
    public void markParsed(OffsetDateTime parsedAt) {
        if (parsedAt == null) {
            throw new IllegalArgumentException("Parsed timestamp is required");
        }
        if (uploadedAt != null && parsedAt.isBefore(uploadedAt)) {
            throw new IllegalArgumentException("Resume cannot be parsed before it was uploaded");
        }
        this.parsedAt = parsedAt;
    }

    public String getId() {
        return id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStorageUri() {
        return storageUri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public OffsetDateTime getParsedAt() {
        return parsedAt;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
}
