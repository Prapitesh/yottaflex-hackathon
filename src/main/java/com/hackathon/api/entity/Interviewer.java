package com.hackathon.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Internal employee who can be assigned to interview panels. */
@Entity
@Table(name = "interviewer")
public class Interviewer {

    @Id
    @NotBlank
    @Size(max = 40)
    @Column(name = "interviewer_id", nullable = false, length = 40)
    private String id;

    @NotBlank
    @Size(max = 160)
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private boolean active = true;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String timezone = "Asia/Kolkata";

    protected Interviewer() {
    }

    public Interviewer(String id, String fullName, String email,
                       Department department, String timezone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        if (timezone != null && !timezone.isBlank()) {
            this.timezone = timezone;
        }
        this.active = true;
    }

    public void updateProfile(String fullName, String email, Department department, String timezone) {
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        if (timezone != null && !timezone.isBlank()) {
            this.timezone = timezone;
        }
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Department getDepartment() { return department; }
    public boolean isActive() { return active; }
    public String getTimezone() { return timezone; }
}
