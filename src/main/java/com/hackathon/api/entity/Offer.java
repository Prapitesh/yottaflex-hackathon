package com.hackathon.api.entity;

import com.hackathon.api.enums.OfferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Employment offer and its approval/response timestamps. */
@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @NotBlank
    @Size(max = 30)
    @Column(name = "offer_id", nullable = false, length = 30)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferStatus status = OfferStatus.DRAFT;

    @Size(max = 120)
    @Column(name = "approved_by", length = 120)
    private String approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Offer() {
    }

    public Offer(String id, Application application, BigDecimal salary,
                 String currency, LocalDate validUntil) {
        this.id = id;
        this.application = application;
        this.salary = salary;
        if (currency != null) {
            this.currency = currency;
        }
        this.validUntil = validUntil;
        this.status = OfferStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public void submitForApproval() {
        this.status = OfferStatus.PENDING_APPROVAL;
    }

    public void approve(String approver, OffsetDateTime approvedAt) {
        if (approver == null || approver.isBlank() || approvedAt == null) {
            throw new IllegalArgumentException("Approver and approval timestamp are required");
        }
        this.approvedBy = approver;
        this.approvedAt = approvedAt;
        this.status = OfferStatus.APPROVED;
    }

    public void issue(OffsetDateTime issuedAt) {
        if (approvedBy == null || approvedAt == null) {
            throw new IllegalStateException("Offer must be approved before it is issued");
        }
        if (issuedAt == null || issuedAt.isBefore(approvedAt)) {
            throw new IllegalArgumentException("Issue timestamp must be on or after approval");
        }
        this.issuedAt = issuedAt;
        this.status = OfferStatus.ISSUED;
    }

    public void recordResponse(OfferStatus response, OffsetDateTime respondedAt) {
        if (response != OfferStatus.ACCEPTED && response != OfferStatus.REJECTED) {
            throw new IllegalArgumentException("Response must be ACCEPTED or REJECTED");
        }
        if (this.status != OfferStatus.ISSUED) {
            throw new IllegalStateException("Only an issued offer can receive a response");
        }
        if (respondedAt == null || (issuedAt != null && respondedAt.isBefore(issuedAt))) {
            throw new IllegalArgumentException("Response timestamp must be on or after issue");
        }
        this.status = response;
        this.respondedAt = respondedAt;
    }

    public void withdraw() { this.status = OfferStatus.WITHDRAWN; }
    public void expire() { this.status = OfferStatus.EXPIRED; }

    @AssertTrue(message = "Approved, issued and accepted offers require approval metadata")
    public boolean isApprovalMetadataValid() {
        return status != OfferStatus.APPROVED
                && status != OfferStatus.ISSUED
                && status != OfferStatus.ACCEPTED
                || (approvedBy != null && !approvedBy.isBlank() && approvedAt != null);
    }

    @AssertTrue(message = "Issued offers require an issue timestamp")
    public boolean isIssueTimestampValid() {
        return status != OfferStatus.ISSUED && status != OfferStatus.ACCEPTED
                || issuedAt != null;
    }

    public String getId() { return id; }
    public Application getApplication() { return application; }
    public BigDecimal getSalary() { return salary; }
    public String getCurrency() { return currency; }
    public OfferStatus getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public OffsetDateTime getRespondedAt() { return respondedAt; }
    public LocalDate getValidUntil() { return validUntil; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
