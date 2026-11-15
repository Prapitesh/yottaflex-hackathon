package com.hackathon.api.entity;

import com.hackathon.api.enums.EmploymentType;
import com.hackathon.api.enums.RequisitionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A business request to hire one or more people for a department.
 * Lifecycle transitions are validated by the service layer.
 */
@Entity
@Table(name = "job_requisition")
public class JobRequisition {

    @Id
    @NotBlank
    @Size(max = 30)
    @Column(name = "requisition_id", nullable = false, length = 30)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String location;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer openings;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "salary_min", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "salary_max", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private RequisitionStatus status = RequisitionStatus.DRAFT;

    @NotBlank
    @Size(max = 120)
    @Column(name = "requested_by", nullable = false, length = 120)
    private String requestedBy;

    @Size(max = 120)
    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "opened_at")
    private OffsetDateTime openedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected JobRequisition() {
        // Required by JPA.
    }

    public JobRequisition(
            String id,
            Department department,
            String title,
            String description,
            EmploymentType employmentType,
            String location,
            Integer openings,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String currency,
            String requestedBy
    ) {
        this.id = id;
        this.department = department;
        this.title = title;
        this.description = description;
        this.employmentType = employmentType;
        this.location = location;
        this.openings = openings;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        if (currency != null) {
            this.currency = currency;
        }
        this.requestedBy = requestedBy;
        this.status = RequisitionStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void updateDetails(
            String title,
            String description,
            EmploymentType employmentType,
            String location,
            Integer openings,
            BigDecimal salaryMin,
            BigDecimal salaryMax
    ) {
        this.title = title;
        this.description = description;
        this.employmentType = employmentType;
        this.location = location;
        this.openings = openings;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
    }

    /**
     * Updates editable draft fields, including the owning department.
     */
    public void updateDetails(
            Department department,
            String title,
            String description,
            EmploymentType employmentType,
            String location,
            Integer openings,
            BigDecimal salaryMin,
            BigDecimal salaryMax
    ) {
        this.department = department;
        updateDetails(title, description, employmentType, location,
                openings, salaryMin, salaryMax);
    }

    public void submitForApproval() {
        this.status = RequisitionStatus.PENDING_APPROVAL;
    }

    public void approve(String approver, OffsetDateTime approvedAt) {
        this.status = RequisitionStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = approvedAt;
        this.openedAt = approvedAt;
    }

    public void reject() {
        // Rejection actor and reason are stored in the audit event, not approval columns.
        this.status = RequisitionStatus.REJECTED;
    }

    public void close(OffsetDateTime closedAt) {
        this.status = RequisitionStatus.CLOSED;
        this.closedAt = closedAt;
    }

    @AssertTrue(message = "Maximum salary must be greater than or equal to minimum salary")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax.compareTo(salaryMin) >= 0;
    }

    @AssertTrue(message = "Approved requisitions require approver and approval timestamp")
    public boolean isApprovalMetadataValid() {
        return status != RequisitionStatus.APPROVED
                || (approvedBy != null && !approvedBy.isBlank() && approvedAt != null);
    }

    @AssertTrue(message = "Closed requisitions require a close timestamp")
    public boolean isCloseTimestampValid() {
        return status != RequisitionStatus.CLOSED || closedAt != null;
    }

    public String getId() { return id; }
    public Department getDepartment() { return department; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public String getLocation() { return location; }
    public Integer getOpenings() { return openings; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public String getCurrency() { return currency; }
    public RequisitionStatus getStatus() { return status; }
    public String getRequestedBy() { return requestedBy; }
    public String getApprovedBy() { return approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getOpenedAt() { return openedAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
