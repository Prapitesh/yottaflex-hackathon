package com.hackathon.api.dto.response.interview;

import com.hackathon.api.enums.InterviewPanelRole;

public record InterviewPanelistResponse(
        String interviewerId,
        String interviewerName,
        String interviewerEmail,
        InterviewPanelRole role
) {
}
