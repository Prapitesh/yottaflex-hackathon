package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Candidate master profile.
 *
 * Skills are deliberately modelled through CandidateSkill rather than a
 * collection here, because the bridge entity stores proficiency and evidence.
 */
@Entity
@Table(
        name = "candidate",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_email",
                        columnNames = "email"
                )
        }
)
public class Candidate {

    @Id
    @NotBlank
    @Size(max = 30)
    @Column(name = "candidate_id", nullable = false, length = 30)
    private String id;

    @NotBlank
    @Size(max = 160)
    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 254)
    @Column(name = "email", nullable = false, length = 254, unique = true)
    private String email;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Size(max = 160)
    @Column(name = "current_location", length = 160)
    private String currentLocation;

    @Min(0)
    @Column(name = "total_experience_months", nullable = false)
    private Integer totalExperienceMonths = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Required by JPA. Use the public constructor in application code. */
    protected Candidate() {
    }

    public Candidate(
            String id,
            String fullName,
            String email,
            String phone,
            String currentLocation,
            Integer totalExperienceMonths
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.currentLocation = currentLocation;
        this.totalExperienceMonths =
                totalExperienceMonths == null ? 0 : totalExperienceMonths;
    }

    @PrePersist
    protected void onCreate() {
        if (totalExperienceMonths == null) {
            totalExperienceMonths = 0;
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public void updateProfile(
            String fullName,
            String email,
            String phone,
            String currentLocation,
            Integer totalExperienceMonths
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.currentLocation = currentLocation;
        this.totalExperienceMonths =
                totalExperienceMonths == null ? 0 : totalExperienceMonths;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public Integer getTotalExperienceMonths() {
        return totalExperienceMonths;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
