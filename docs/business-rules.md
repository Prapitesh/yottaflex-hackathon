# Recruitment ATS — Business Rules

## Lifecycle and authority
- A requisition may move DRAFT -> PENDING_APPROVAL -> APPROVED or REJECTED. APPROVED may move to CLOSED. Only an authorized approver may approve/reject; only approved requisitions accept applications.
- An application starts at APPLIED. Every stage change must append application_stage_history and audit_event in the same transaction as the state update.
- Allowed progression: APPLIED -> SCREENING; SCREENING -> SHORTLISTED or REJECTED; SHORTLISTED -> INTERVIEW or REJECTED; INTERVIEW -> SELECTED or REJECTED; SELECTED -> OFFERED; OFFERED -> HIRED or REJECTED. WITHDRAWN may be entered from any non-terminal stage. No silent backward transitions; corrections require an explicit audited correction event.
- Interview scheduling must check every assigned active interviewer for overlapping scheduled intervals. Intervals overlap when new_start < existing_end AND new_end > existing_start. A conflict blocks ordinary scheduling; seeded anomaly cases are retained and labelled rather than silently altered.
- Feedback is submitted only by an interviewer assigned to that interview. An interview stage cannot be completed/advanced until required panel feedback exists.
- Screening uses only job-related criteria configured for that requisition. Mandatory requirements are gates; weighted preferred criteria contribute to a 0–100 score. Do not use protected attributes or demographic proxies.
- An offer may be issued only after required selection and approval checks. Salary must be positive and normally within the requisition's approved salary band. Out-of-band seeded records are explicitly labelled as anomalies and must be flagged.
- Duplicate candidate/requisition applications are detected by grouping on (requisition_id,candidate_id). The schema intentionally retains duplicates for audit and ground-truth evaluation.
- Cross-row issues (interviewer overlap, missing feedback, salary outside band, active applications on a closed role) are service/validation rules, not database CHECK constraints.

## Required seeded anomaly labels
1. INTERVIEWER_UNAVAILABLE — interview overlaps an existing scheduled interview for at least one assigned interviewer.
2. DUPLICATE_APPLICATION — same candidate has more than one application for the same requisition.
3. MISSING_FEEDBACK_STAGE_ADVANCED — application advanced beyond interview without required feedback.
4. OFFER_OUTSIDE_APPROVED_BAND — offer salary is outside the requisition min/max.
5. CLOSED_REQUISITION_ACTIVE_APPLICATIONS — requisition is closed while one or more applications remain active.

Ground truth must be stored separately from model/input features. Keep source records unchanged and attach issue_type, ground_truth_label, expected_decision and evidence.

## Data integrity
- All IDs are generated deterministically using the specified prefixes.
- Use UTC timestamps in persistence; display in the user's timezone.
- Use transactions for multi-record workflows.
- Never delete or overwrite workflow history.
- Baseline data must be valid before labelled anomalies are added.

## Screening implementation semantics
- A screening run is allowed only while an application is in APPLIED and moves it to SCREENING; the run stores score and decision, and appends stage history and audit evidence in one transaction.
- REQUIRED_SKILL and PREFERRED_SKILL rules compare the candidate's recorded skill evidence. A rule threshold, when present, is the minimum years of experience for that skill.
- MIN_EXPERIENCE thresholds are expressed in years and compared with total candidate experience months converted to years.
- Mandatory criteria are pass/fail gates. Non-mandatory skill and experience criteria contribute their configured weights, normalized to a 0-100 score.
- Unverified evidence and unsupported LOCATION/CUSTOM rules result in REVIEW rather than an automatic rejection. The current schema does not yet store a target value for LOCATION/CUSTOM comparisons; these need an explicit comparison field before they can be evaluated automatically.
- No active screening rules results in REVIEW with a score of 0 until criteria are configured.

## Interview scheduling and lifecycle
- Interviews may be scheduled only for applications in SHORTLISTED or INTERVIEW. The first scheduled interview advances SHORTLISTED to INTERVIEW and appends stage history; later rounds leave the application in INTERVIEW.
- Every panel must contain unique interviewer IDs and exactly one LEAD. All assigned interviewers must exist and be active.
- An interview interval must have end strictly after start and must begin in the future.
- An interviewer is unavailable when an active SCHEDULED or RESCHEDULED interview overlaps the requested interval. Adjacent intervals are allowed because overlap uses start < existing end and end > existing start.
- Interviewer rows are locked in sorted ID order during schedule/reschedule transactions before availability checks to reduce concurrent double-booking risk and avoid inconsistent lock ordering.
- Reschedule checks availability excluding the interview being moved. Cancelled, completed and no-show interviews do not block future availability.
- Schedule, reschedule, cancel and complete actions create audit events. The first schedule also creates application stage history.
- Database-backed integration tests must later validate the repository JPQL query, pessimistic locking and actual PostgreSQL timestamp behavior.

## Interview feedback and selection gate
- Feedback may be submitted only after the associated interview is COMPLETED and only by an interviewer assigned to that interview.
- Each assigned interviewer may submit exactly one feedback record per interview. The database uniqueness constraint on (interview_id, interviewer_id) remains the final duplicate safeguard.
- Feedback contains a numeric score from 0 to 10, a structured recommendation, strengths and optional concerns. Submission is audited with the submitting interviewer as actor.
- An application can move from INTERVIEW to SELECTED only when it has at least one completed interview, every non-cancelled interview is completed, and every assigned panel member has submitted feedback for each completed interview.
- Cancelled interviews do not require feedback. Scheduled, rescheduled or no-show interviews block selection until resolved.
- Selection appends application stage history and an audit event. Feedback does not automatically select or reject a candidate; the authorized recruiter makes the decision after reviewing the evidence.
- The API currently accepts interviewer ID and recruiter actor in request payloads because authentication is not yet implemented; a secured deployment must derive these identities from the authenticated principal.

## Offer lifecycle and approval gate
- Offers may be drafted only for applications in SELECTED.
- Offer salary must be positive and fall within the linked requisition's approved minimum/maximum salary band; offer currency must match the requisition currency.
- Only one active offer (DRAFT, PENDING_APPROVAL, APPROVED or ISSUED) may exist per application. Closed historical offers remain available for audit.
- Offer progression is DRAFT -> PENDING_APPROVAL -> APPROVED -> ISSUED -> ACCEPTED or REJECTED. Every action is audited; invalid status transitions are rejected.
- An offer must have a future-or-current valid_until date. Approval and issue are blocked after that date. A candidate response received after the validity date marks the issued offer EXPIRED.
- Issuing an approved offer moves the application SELECTED -> OFFERED and appends application_stage_history in the same transaction.
- ACCEPTED moves OFFERED -> HIRED; REJECTED moves OFFERED -> REJECTED. Both transitions append stage history and audit evidence.
- The current API accepts actor identities in request payloads because authentication is not yet implemented; production authorization must derive actors and permissions from the authenticated principal and enforce separation of duties between offer creator and approver where policy requires it.

## Recruiter application decisions
- A recruiter may move a SCREENING application to SHORTLISTED or REJECTED after reviewing the screening evidence.
- A recruiter may reject an application while it is in SHORTLISTED or INTERVIEW; moving to SHORTLISTED is only valid from SCREENING.
- Recruiter decisions require an actor and reason, and update the application stage, append application_stage_history and write an audit_event in one transaction.
- Clients cannot directly set server-controlled stages such as APPLIED, SCREENING, INTERVIEW, SELECTED, OFFERED or HIRED through the recruiter decision endpoint.
- If a candidate withdraws an application in SELECTED or OFFERED, any active linked offer (DRAFT, PENDING_APPROVAL, APPROVED or ISSUED) is moved to WITHDRAWN and audited in the same transaction so offer and application lifecycles remain consistent.
