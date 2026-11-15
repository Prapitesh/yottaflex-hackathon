package com.hackathon.api.entity;

import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.ScreeningDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationEntityTest {

    @Test
    void startsAtAppliedStageAndSetsTimestamps() {
        Application application = new Application("APP-REQ-1-CAND-1", null, null, null);
        application.onCreate();

        assertEquals(ApplicationStage.APPLIED, application.getStage());
        assertNotNull(application.getAppliedAt());
        assertEquals(application.getAppliedAt(), application.getLastStageChangedAt());
    }

    @Test
    void recordsScreeningScoreAndDecision() {
        Application application = new Application("APP-REQ-1-CAND-1", null, null, null);
        application.recordScreening(new BigDecimal("82.50"), ScreeningDecision.PASS);

        assertEquals(new BigDecimal("82.50"), application.getScreeningScore());
        assertEquals(ScreeningDecision.PASS, application.getScreeningDecision());
    }

    @Test
    void rejectsOutOfRangeScreeningScore() {
        Application application = new Application("APP-REQ-1-CAND-1", null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> application.recordScreening(new BigDecimal("101"), ScreeningDecision.PASS));
    }

    @Test
    void changesStageAndTimestamp() {
        Application application = new Application("APP-REQ-1-CAND-1", null, null, null);
        OffsetDateTime changedAt = OffsetDateTime.now();

        application.changeStage(ApplicationStage.SHORTLISTED, changedAt);

        assertEquals(ApplicationStage.SHORTLISTED, application.getStage());
        assertEquals(changedAt, application.getLastStageChangedAt());
    }
}
