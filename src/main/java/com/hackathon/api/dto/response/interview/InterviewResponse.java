package com.hackathon.api.dto.response.interview;

import com.hackathon.api.enums.InterviewMode;
import com.hackathon.api.enums.InterviewStatus;
import com.hackathon.api.enums.InterviewType;
import java.time.OffsetDateTime;
import java.util.List;

public record InterviewResponse(
        String id,
        String applicationId,
        InterviewType interviewType,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        InterviewMode mode,
        String meetingLocation,
        InterviewStatus status,
        List<InterviewPanelistResponse> panel
) {
    public InterviewResponse {
        panel = panel == null ? List.of() : List.copyOf(panel);
    }
}
