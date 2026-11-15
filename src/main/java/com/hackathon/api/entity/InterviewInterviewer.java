package com.hackathon.api.entity;

import com.hackathon.api.enums.InterviewPanelRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/** Join entity assigning interviewers to an interview and recording their panel role. */
@Entity
@Table(name = "interview_interviewer")
public class InterviewInterviewer {

    @EmbeddedId
    private InterviewInterviewerId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("interviewId")
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("interviewerId")
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewPanelRole role;

    protected InterviewInterviewer() {
    }

    public InterviewInterviewer(Interview interview, Interviewer interviewer, InterviewPanelRole role) {
        if (interview == null || interviewer == null) {
            throw new IllegalArgumentException("Interview and interviewer are required");
        }
        this.interview = interview;
        this.interviewer = interviewer;
        this.id = new InterviewInterviewerId(interview.getId(), interviewer.getId());
        this.role = role;
    }

    public InterviewInterviewerId getId() { return id; }
    public Interview getInterview() { return interview; }
    public Interviewer getInterviewer() { return interviewer; }
    public InterviewPanelRole getRole() { return role; }
    public void changeRole(InterviewPanelRole role) { this.role = role; }
}
