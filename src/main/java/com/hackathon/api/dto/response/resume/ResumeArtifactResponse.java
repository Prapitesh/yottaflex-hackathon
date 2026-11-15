package com.hackathon.api.dto.response.resume;
import java.time.OffsetDateTime;
public record ResumeArtifactResponse(String id,String candidateId,String applicationId,String fileName,String storageUri,String mimeType,String checksumSha256,OffsetDateTime parsedAt,OffsetDateTime uploadedAt) {}