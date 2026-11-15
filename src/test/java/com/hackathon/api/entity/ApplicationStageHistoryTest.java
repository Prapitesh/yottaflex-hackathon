package com.hackathon.api.entity;

import com.hackathon.api.enums.ApplicationStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationStageHistoryTest {

    @Test
    void capturesTransitionReasonAndActor() {
        ApplicationStageHistory history = new ApplicationStageHistory(
                null, ApplicationStage.APPLIED, ApplicationStage.SCREENING,
                "Recruiter", "Application passed initial review"
        );
        history.onCreate();

        assertEquals(ApplicationStage.APPLIED, history.getFromStage());
        assertEquals(ApplicationStage.SCREENING, history.getToStage());
        assertEquals("Recruiter", history.getChangedBy());
        assertNotNull(history.getChangedAt());
    }
}
