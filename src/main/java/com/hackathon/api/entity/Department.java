package com.hackathon.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * JPA mapping for the department master table.
 *
 * A department owns requisitions and may be associated with interviewers.
 * Relationships will be connected after those entity classes are introduced.
 */
@Entity
@Table(
        name = "department",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_name",
                        columnNames = "name"
                )
        }
)
public class Department {

    @Id
    @NotBlank
    @Size(max = 30)
    @Column(name = "department_id", nullable = false, length = 30)
    private String id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120, unique = true)
    private String name;

    @Size(max = 40)
    @Column(name = "cost_center", length = 40)
    private String costCenter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Required by JPA. Application code should use the public constructor.
     */
    protected Department() {
    }

    public Department(String id, String name, String costCenter) {
        this.id = id;
        this.name = name;
        this.costCenter = costCenter;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public void updateDetails(String name, String costCenter) {
        this.name = name;
        this.costCenter = costCenter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
