package com.hackathon.api.controller;

import com.hackathon.api.dto.response.screening.ScreeningResult;
import com.hackathon.api.enums.ApplicationStage;
import com.hackathon.api.enums.ScreeningDecision;
import com.hackathon.api.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScreeningControllerTest {

    @Test
    void screenDelegatesApplicationIdAndReturnsResult() {
        ScreeningService service = applicationId -> new ScreeningResult(
                applicationId, new BigDecimal("85.00"),
                ScreeningDecision.PASS, ApplicationStage.SCREENING,
                List.of("Matched Java skill"));
        ScreeningController controller = new ScreeningController(service);

        var response = controller.screen("APP-REQ-1-CAND-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APP-REQ-1-CAND-1", response.getBody().applicationId());
        assertEquals(new BigDecimal("85.00"), response.getBody().score());
        assertEquals(ScreeningDecision.PASS, response.getBody().decision());
    }
}
