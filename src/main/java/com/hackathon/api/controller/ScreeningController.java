package com.hackathon.api.controller;

import com.hackathon.api.dto.response.screening.ScreeningResult;
import com.hackathon.api.service.ScreeningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP endpoint for running the configured screening rules on an application.
 */
@RestController
@RequestMapping("/api/v1/applications")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @PostMapping("/{applicationId}/screen")
    public ResponseEntity<ScreeningResult> screen(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(screeningService.screenApplication(applicationId));
    }
}
