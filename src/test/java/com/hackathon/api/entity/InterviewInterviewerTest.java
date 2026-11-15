package com.hackathon.api.entity;

import com.hackathon.api.enums.InterviewMode;
import com.hackathon.api.enums.InterviewPanelRole;
import com.hackathon.api.enums.InterviewType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InterviewInterviewerTest {

    @Test
    void buildsCompositeKeyForPanelAssignment() {
        OffsetDateTime start = OffsetDateTime.now();
        Interview interview = new Interview("INT-1", null, InterviewType.TECHNICAL,
                start, start.plusHours(1), InterviewMode.ONLINE, null);
        Interviewer interviewer = new Interviewer(
                "INTV-1", "Panel Lead", "lead@example.com", null, null);

        InterviewInterviewer assignment = new InterviewInterviewer(
                interview, interviewer, InterviewPanelRole.LEAD);

        assertEquals("INT-1", assignment.getId().getInterviewId());
        assertEquals("INTV-1", assignment.getId().getInterviewerId());
        assertEquals(InterviewPanelRole.LEAD, assignment.getRole());
    }

    @Test
    void canChangePanelRole() {
        OffsetDateTime start = OffsetDateTime.now();
        Interview interview = new Interview("INT-2", null, InterviewType.HR,
                start, start.plusHours(1), InterviewMode.PHONE, null);
        Interviewer interviewer = new Interviewer(
                "INTV-2", "Panel Member", "member@example.com", null, null);
        InterviewInterviewer assignment = new InterviewInterviewer(
                interview, interviewer, InterviewPanelRole.OBSERVER);

        assignment.changeRole(InterviewPanelRole.PANELIST);

        assertEquals(InterviewPanelRole.PANELIST, assignment.getRole());
    }
}
