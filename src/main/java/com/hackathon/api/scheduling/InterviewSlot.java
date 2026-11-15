package com.hackathon.api.scheduling;

import java.time.Instant;
import java.util.Objects;

public record InterviewSlot(String interviewerId, Instant start, Instant end) {
    public InterviewSlot {
        Objects.requireNonNull(interviewerId, "interviewerId is required");
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(end, "end is required");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Interview end must be after start");
        }
    }
}

