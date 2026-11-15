package com.hackathon.api.entity;

import com.hackathon.api.enums.InterviewMode;
import com.hackathon.api.enums.InterviewStatus;
import com.hackathon.api.enums.InterviewType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InterviewTest {

    @Test
    void createsScheduledInterviewAndValidatesTimeRange() {
        OffsetDateTime start = OffsetDateTime.parse("2026-11-16T10:00:00+05:30");
        Interview interview = new Interview(
                "INT-2026-000001", null, InterviewType.TECHNICAL,
                start, start.plusHours(1), InterviewMode.ONLINE, "Video call"
        );

        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
        assertTrue(interview.isScheduleValid());
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        OffsetDateTime start = OffsetDateTime.parse("2026-11-16T10:00:00+05:30");
        Interview interview = new Interview(
                "INT-2026-000002", null, InterviewType.HR,
                start, start.minusMinutes(15), InterviewMode.PHONE, null
        );

        assertFalse(interview.isScheduleValid());
    }

    @Test
    void reschedulingChangesStatusAndTimes() {
        OffsetDateTime start = OffsetDateTime.parse("2026-11-16T10:00:00+05:30");
        Interview interview = new Interview(
                "INT-2026-000003", null, InterviewType.MANAGERIAL,
                start, start.plusHours(1), InterviewMode.ONSITE, "Office"
        );
        OffsetDateTime newStart = start.plusDays(1);

        interview.reschedule(newStart, newStart.plusHours(1));

        assertEquals(InterviewStatus.RESCHEDULED, interview.getStatus());
        assertEquals(newStart, interview.getScheduledStart());
    }

    @Test
    void canCompleteOrCancelInterview() {
        OffsetDateTime start = OffsetDateTime.now();
        Interview interview = new Interview(
                "INT-2026-000004", null, InterviewType.SCREENING,
                start, start.plusMinutes(30), InterviewMode.ONLINE, null
        );

        interview.complete();
        assertEquals(InterviewStatus.COMPLETED, interview.getStatus());

        interview.cancel();
        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());
    }
}
