# Data Dictionary

All timestamps are PostgreSQL TIMESTAMPTZ (UTC at rest). Money is NUMERIC(12,2) with ISO-4217 currency code (default INR). IDs are strings to preserve required prefixes.

| Entity | Primary key | Important fields | Relationships / rules |
|---|---|---|---|
| Department | department_id | name, cost_center | One department has many requisitions and interviewers; name unique |
| JobRequisition | requisition_id | department_id, title, employment_type, location, openings, salary_min/max, currency, status, approval metadata | Many-to-one Department; salary_max >= salary_min; approval metadata required when APPROVED |
| Candidate | candidate_id | full_name, email, phone, current_location, total_experience_months | Email unique; experience >= 0 |
| Application | application_id | requisition_id, candidate_id, resume_id, stage, screening_score, screening_decision, applied_at | Many-to-one Candidate and JobRequisition; duplicate pair is detected, not constrained away |
| ResumeArtifact | resume_id | candidate_id, application_id, file_name, storage_uri, mime_type, checksum_sha256, parsed_at | Belongs to Candidate; optionally linked to Application; checksum is SHA-256 |
| Skill | skill_id | name, category | Skill name unique |
| CandidateSkill | candidate_id + skill_id | proficiency, years_experience, evidence_source, verified | Bridge: Candidate many-to-many Skill |
| ScreeningRule | rule_id | requisition_id, skill_id, rule_type, weight, threshold, is_mandatory, effective_from/to | Requisition-specific rule; skill optional for non-skill rule |
| Interview | interview_id | application_id, interview_type, scheduled_start/end, mode, status | Many-to-one Application; end > start |
| Interviewer | interviewer_id | full_name, email, department_id, active, timezone | Can participate in many interviews |
| Feedback | feedback_id | interview_id, interviewer_id, score, recommendation, strengths, concerns, submitted_at | One feedback per interviewer per interview; score 0–10 |
| Offer | offer_id | application_id, salary, currency, status, approval/issue/response dates | Belongs to Application; approval metadata required before approved/issued/accepted |

## Supporting traceability tables
- interview_interviewer: many-to-many bridge assigning interviewers to interviews, with panel role.
- application_stage_history: append-only stage transition record.
- audit_event: append-only event payload with actor and timestamp.
- data_issue: explicit anomaly record with issue_type, ground_truth_label, expected_decision and evidence.

## Required ID formats
- Requisition: REQ-2026-000001
- Candidate: CAND-000001
- Application: APP-<requisition-id>-<candidate-id>
- Interview: INT-2026-000001
- Feedback: FB-<interview-id>-<interviewer-id>
- Offer: OFF-2026-000001
- Department, Skill, Interviewer, ScreeningRule, ResumeArtifact: stable prefixed IDs (DEPT-, SKILL-, INTVR-, RULE-, RES-).
