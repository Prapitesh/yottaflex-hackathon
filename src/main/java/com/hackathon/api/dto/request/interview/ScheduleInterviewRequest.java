package com.hackathon.api.dto.request.interview;

import com.hackathon.api.enums.InterviewMode;
import com.hackathon.api.enums.InterviewType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

public record ScheduleInterviewRequest(
        @NotBlank(message = "Application ID is required") @Size(max = 100)
        String applicationId,
        @NotNull(message = "Interview type is required")
        InterviewType interviewType,
        @NotNull(message = "Start time is required")
        OffsetDateTime scheduledStart,
        @NotNull(message = "End time is required")
        OffsetDateTime scheduledEnd,
        @NotNull(message = "Interview mode is required")
        InterviewMode mode,
        @Size(max = 1000)
        String meetingLocation,
        @NotBlank(message = "Actor is required") @Size(max = 120)
        String actor,
        @NotEmpty(message = "At least one interviewer must be assigned")
        List<@Valid InterviewPanelistRequest> panel
) {
}
