package com.hackathon.api.entity;

import com.hackathon.api.enums.OfferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OfferTest {

    @Test
    void offerMustBeApprovedBeforeIssue() {
        Offer offer = new Offer("OFF-2026-000001", null, new BigDecimal("800000"),
                "INR", LocalDate.of(2026, 12, 1));

        assertThrows(IllegalStateException.class, () -> offer.issue(OffsetDateTime.now()));
    }

    @Test
    void approvalThenIssueThenAcceptancePreservesTimestamps() {
        Offer offer = new Offer("OFF-2026-000002", null, new BigDecimal("900000"),
                "INR", LocalDate.of(2026, 12, 1));
        OffsetDateTime approvedAt = OffsetDateTime.now();
        OffsetDateTime issuedAt = approvedAt.plusMinutes(1);
        OffsetDateTime respondedAt = issuedAt.plusDays(1);

        offer.approve("Finance Approver", approvedAt);
        offer.issue(issuedAt);
        offer.recordResponse(OfferStatus.ACCEPTED, respondedAt);

        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        assertEquals("Finance Approver", offer.getApprovedBy());
        assertEquals(approvedAt, offer.getApprovedAt());
        assertEquals(issuedAt, offer.getIssuedAt());
        assertEquals(respondedAt, offer.getRespondedAt());
        assertTrue(offer.isApprovalMetadataValid());
        assertTrue(offer.isIssueTimestampValid());
    }

    @Test
    void cannotAcceptBeforeOfferIsIssued() {
        Offer offer = new Offer("OFF-2026-000003", null, new BigDecimal("700000"),
                "INR", null);

        assertThrows(IllegalStateException.class,
                () -> offer.recordResponse(OfferStatus.ACCEPTED, OffsetDateTime.now()));
    }

    @Test
    void initializesDraftAndCreatedTimestamp() {
        Offer offer = new Offer("OFF-2026-000004", null, new BigDecimal("700000"),
                "INR", null);
        offer.onCreate();

        assertEquals(OfferStatus.DRAFT, offer.getStatus());
        assertNotNull(offer.getCreatedAt());
    }
}
