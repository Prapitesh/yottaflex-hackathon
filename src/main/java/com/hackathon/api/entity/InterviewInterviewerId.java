package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InterviewInterviewerId implements Serializable {

    @Column(name = "interview_id", nullable = false, length = 30)
    private String interviewId;

    @Column(name = "interviewer_id", nullable = false, length = 40)
    private String interviewerId;

    protected InterviewInterviewerId() {
    }

    public InterviewInterviewerId(String interviewId, String interviewerId) {
        this.interviewId = interviewId;
        this.interviewerId = interviewerId;
    }

    public String getInterviewId() { return interviewId; }
    public String getInterviewerId() { return interviewerId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof InterviewInterviewerId that)) return false;
        return Objects.equals(interviewId, that.interviewId)
                && Objects.equals(interviewerId, that.interviewerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interviewId, interviewerId);
    }
}
