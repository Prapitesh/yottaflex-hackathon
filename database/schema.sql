[Reading 208 lines from start (total: 208 lines, 0 remaining)]

-- CandidateWorkspace Recruitment ATS schema
-- PostgreSQL 14+. Apply to the dedicated yottaflex-hackathon database only.
-- Core domain: 12 required entities. Supporting tables preserve workflow/audit evidence.

CREATE TABLE IF NOT EXISTS department (
  department_id VARCHAR(30) PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  cost_center VARCHAR(40),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS job_requisition (
  requisition_id VARCHAR(30) PRIMARY KEY,
  department_id VARCHAR(30) NOT NULL REFERENCES department(department_id),
  title VARCHAR(160) NOT NULL,
  description TEXT NOT NULL,
  employment_type VARCHAR(20) NOT NULL CHECK (employment_type IN ('FULL_TIME','PART_TIME','CONTRACT','INTERN')),
  location VARCHAR(160) NOT NULL,
  openings INTEGER NOT NULL CHECK (openings > 0),
  salary_min NUMERIC(12,2) NOT NULL CHECK (salary_min >= 0),
  salary_max NUMERIC(12,2) NOT NULL CHECK (salary_max >= salary_min),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  status VARCHAR(24) NOT NULL CHECK (status IN ('DRAFT','PENDING_APPROVAL','APPROVED','REJECTED','CLOSED')),
  requested_by VARCHAR(120) NOT NULL,
  approved_by VARCHAR(120),
  approved_at TIMESTAMPTZ,
  opened_at TIMESTAMPTZ,
  closed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((status <> 'APPROVED') OR (approved_by IS NOT NULL AND approved_at IS NOT NULL)),
  CHECK ((status <> 'CLOSED') OR closed_at IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS candidate (
  candidate_id VARCHAR(30) PRIMARY KEY,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  phone VARCHAR(30),
  current_location VARCHAR(160),
  total_experience_months INTEGER NOT NULL DEFAULT 0 CHECK (total_experience_months >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS resume_artifact (
  resume_id VARCHAR(40) PRIMARY KEY,
  candidate_id VARCHAR(30) NOT NULL REFERENCES candidate(candidate_id),
  application_id VARCHAR(80),
  file_name VARCHAR(255) NOT NULL,
  storage_uri TEXT NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  checksum_sha256 VARCHAR(64) NOT NULL,
  parsed_at TIMESTAMPTZ,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS skill (
  skill_id VARCHAR(30) PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS candidate_skill (
  candidate_id VARCHAR(30) NOT NULL REFERENCES candidate(candidate_id),
  skill_id VARCHAR(30) NOT NULL REFERENCES skill(skill_id),
  proficiency VARCHAR(16) NOT NULL CHECK (proficiency IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')),
  years_experience NUMERIC(4,1) NOT NULL DEFAULT 0 CHECK (years_experience >= 0),
  evidence_source VARCHAR(30) NOT NULL CHECK (evidence_source IN ('RESUME','ASSESSMENT','INTERVIEW','DECLARED')),
  verified BOOLEAN NOT NULL DEFAULT false,
  PRIMARY KEY (candidate_id, skill_id)
);

CREATE TABLE IF NOT EXISTS screening_rule (
  rule_id VARCHAR(40) PRIMARY KEY,
  requisition_id VARCHAR(30) NOT NULL REFERENCES job_requisition(requisition_id),
  skill_id VARCHAR(30) REFERENCES skill(skill_id),
  rule_type VARCHAR(24) NOT NULL CHECK (rule_type IN ('REQUIRED_SKILL','PREFERRED_SKILL','MIN_EXPERIENCE','LOCATION','CUSTOM')),
  weight NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (weight >= 0 AND weight <= 100),
  threshold NUMERIC(8,2),
  is_mandatory BOOLEAN NOT NULL DEFAULT false,
  effective_from DATE NOT NULL,
  effective_to DATE,
  CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE IF NOT EXISTS application (
  application_id VARCHAR(100) PRIMARY KEY,
  requisition_id VARCHAR(30) NOT NULL REFERENCES job_requisition(requisition_id),
  candidate_id VARCHAR(30) NOT NULL REFERENCES candidate(candidate_id),
  resume_id VARCHAR(40) REFERENCES resume_artifact(resume_id),
  stage VARCHAR(24) NOT NULL CHECK (stage IN ('APPLIED','SCREENING','SHORTLISTED','INTERVIEW','SELECTED','REJECTED','WITHDRAWN','OFFERED','HIRED')),
  screening_score NUMERIC(5,2) CHECK (screening_score BETWEEN 0 AND 100),
  screening_decision VARCHAR(20) CHECK (screening_decision IN ('PASS','FAIL','REVIEW')),
  applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_stage_changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  -- Deliberately no UNIQUE(requisition_id,candidate_id): duplicates must remain observable and labelable.
  CHECK (application_id LIKE 'APP-%')
);

ALTER TABLE resume_artifact DROP CONSTRAINT IF EXISTS fk_resume_application;
ALTER TABLE resume_artifact ADD CONSTRAINT fk_resume_application
  FOREIGN KEY (application_id) REFERENCES application(application_id) DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE IF NOT EXISTS interviewer (
  interviewer_id VARCHAR(40) PRIMARY KEY,
  full_name VARCHAR(160) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  department_id VARCHAR(30) REFERENCES department(department_id),
  active BOOLEAN NOT NULL DEFAULT true,
  timezone VARCHAR(60) NOT NULL DEFAULT 'Asia/Kolkata'
);

CREATE TABLE IF NOT EXISTS interview (
  interview_id VARCHAR(30) PRIMARY KEY,
  application_id VARCHAR(100) NOT NULL REFERENCES application(application_id),
  interview_type VARCHAR(24) NOT NULL CHECK (interview_type IN ('SCREENING','TECHNICAL','MANAGERIAL','HR','FINAL')),
  scheduled_start TIMESTAMPTZ NOT NULL,
  scheduled_end TIMESTAMPTZ NOT NULL,
  mode VARCHAR(16) NOT NULL CHECK (mode IN ('ONLINE','ONSITE','PHONE')),
  meeting_location TEXT,
  status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','RESCHEDULED','NO_SHOW')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (scheduled_end > scheduled_start)
);

CREATE TABLE IF NOT EXISTS interview_interviewer (
  interview_id VARCHAR(30) NOT NULL REFERENCES interview(interview_id),
  interviewer_id VARCHAR(40) NOT NULL REFERENCES interviewer(interviewer_id),
  role VARCHAR(20) NOT NULL CHECK (role IN ('LEAD','PANELIST','OBSERVER')),
  PRIMARY KEY (interview_id, interviewer_id)
);

CREATE TABLE IF NOT EXISTS feedback (
  feedback_id VARCHAR(60) PRIMARY KEY,
  interview_id VARCHAR(30) NOT NULL REFERENCES interview(interview_id),
  interviewer_id VARCHAR(40) NOT NULL REFERENCES interviewer(interviewer_id),
  score NUMERIC(4,2) NOT NULL CHECK (score BETWEEN 0 AND 10),
  recommendation VARCHAR(20) NOT NULL CHECK (recommendation IN ('STRONG_YES','YES','HOLD','NO','STRONG_NO')),
  strengths TEXT NOT NULL,
  concerns TEXT,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (interview_id, interviewer_id)
);

CREATE TABLE IF NOT EXISTS offer (
  offer_id VARCHAR(30) PRIMARY KEY,
  application_id VARCHAR(100) NOT NULL REFERENCES application(application_id),
  salary NUMERIC(12,2) NOT NULL CHECK (salary > 0),
  currency VARCHAR(3) NOT NULL DEFAULT 'INR',
  status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT','PENDING_APPROVAL','APPROVED','ISSUED','ACCEPTED','REJECTED','WITHDRAWN','EXPIRED')),
  approved_by VARCHAR(120),
  approved_at TIMESTAMPTZ,
  issued_at TIMESTAMPTZ,
  responded_at TIMESTAMPTZ,
  valid_until DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((status NOT IN ('APPROVED','ISSUED','ACCEPTED')) OR (approved_by IS NOT NULL AND approved_at IS NOT NULL))
);

-- Supporting traceability tables: immutable workflow events and explicit detected issues.
CREATE TABLE IF NOT EXISTS application_stage_history (
  history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  application_id VARCHAR(100) NOT NULL REFERENCES application(application_id),
  from_stage VARCHAR(24),
  to_stage VARCHAR(24) NOT NULL,
  changed_by VARCHAR(120) NOT NULL,
  reason TEXT NOT NULL,
  changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS audit_event (
  event_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  aggregate_type VARCHAR(60) NOT NULL,
  aggregate_id VARCHAR(100) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  actor VARCHAR(120) NOT NULL,
  event_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS data_issue (
  issue_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  issue_type VARCHAR(80) NOT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id VARCHAR(100) NOT NULL,
  ground_truth_label VARCHAR(40) NOT NULL,
  expected_decision TEXT NOT NULL,
  details JSONB NOT NULL DEFAULT '{}'::jsonb,
  detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_requisition_status_department ON job_requisition(status, department_id);
CREATE INDEX IF NOT EXISTS idx_application_requisition_stage ON application(requisition_id, stage);
CREATE INDEX IF NOT EXISTS idx_application_candidate ON application(candidate_id);
CREATE INDEX IF NOT EXISTS idx_interview_schedule ON interview(scheduled_start, scheduled_end);
CREATE INDEX IF NOT EXISTS idx_feedback_interview ON feedback(interview_id);
CREATE INDEX IF NOT EXISTS idx_offer_application_status ON offer(application_id, status);
CREATE INDEX IF NOT EXISTS idx_audit_aggregate ON audit_event(aggregate_type, aggregate_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_data_issue_type ON data_issue(issue_type, ground_truth_label);

-- NOTE: interviewer overlap, duplicate applications, missing feedback vs stage,
-- and salary-vs-requisition band are cross-row business validations, not simple CHECK constraints.

-- Prevent concurrent requests from creating multiple active offers for one application.
CREATE UNIQUE INDEX IF NOT EXISTS uq_offer_one_active_per_application
  ON offer(application_id)
  WHERE status IN ('DRAFT','PENDING_APPROVAL','APPROVED','ISSUED');

[executed on device: DESKTOP-8BCJIIV (c27eb905-f499-4290-a6f7-fc6e598ccef1)]
