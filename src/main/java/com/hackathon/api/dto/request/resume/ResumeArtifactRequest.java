package com.hackathon.api.dto.request.resume;
import jakarta.validation.constraints.*;
public record ResumeArtifactRequest(@NotBlank String candidateId,@Size(max=80) String applicationId,@NotBlank @Size(max=255) String fileName,@NotBlank String storageUri,@NotBlank @Size(max=100) String mimeType,@NotBlank @Pattern(regexp="^[a-fA-F0-9]{64}$") String checksumSha256) {}