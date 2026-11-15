package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Master catalogue of skills used by requisition screening and candidate
 * profiles. Candidate-specific proficiency belongs in CandidateSkill.
 */
@Entity
@Table(
        name = "skill",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_skill_name",
                        columnNames = "name"
                )
        }
)
public class Skill {

    @Id
    @NotBlank
    @Size(max = 30)
    @Column(name = "skill_id", nullable = false, length = 30)
    private String id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @NotBlank
    @Size(max = 60)
    @Column(name = "category", nullable = false, length = 60)
    private String category;

    /** Required by JPA. Use the public constructor in application code. */
    protected Skill() {
    }

    public Skill(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public void updateDetails(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
