package com.hackathon.api.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterviewerTest {

    @Test
    void createsActiveInterviewerWithDefaultTimezone() {
        Interviewer interviewer = new Interviewer(
                "INTV-1", "Riya Rao", "riya@example.com", null, null
        );

        assertTrue(interviewer.isActive());
        assertEquals("Asia/Kolkata", interviewer.getTimezone());
    }

    @Test
    void canDeactivateAndReactivate() {
        Interviewer interviewer = new Interviewer(
                "INTV-2", "Arun Das", "arun@example.com", null, "Asia/Kolkata"
        );

        interviewer.deactivate();
        assertFalse(interviewer.isActive());

        interviewer.activate();
        assertTrue(interviewer.isActive());
    }

    @Test
    void updatesProfile() {
        Interviewer interviewer = new Interviewer(
                "INTV-3", "Old Name", "old@example.com", null, "Asia/Kolkata"
        );

        interviewer.updateProfile("New Name", "new@example.com", null, "Asia/Singapore");

        assertEquals("New Name", interviewer.getFullName());
        assertEquals("new@example.com", interviewer.getEmail());
        assertEquals("Asia/Singapore", interviewer.getTimezone());
    }
}
