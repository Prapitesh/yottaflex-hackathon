# CandidateWorkspace — Recruitment Workflow ATS

A three-tier recruitment workflow application built for the YottaFlex hackathon.

## Architecture

- Frontend: React 19 + Vite dashboard.
- Backend: Java 21 + Spring Boot REST API, layered controller/service/repository architecture.
- Database: PostgreSQL with explicit schema, foreign keys, checks, indexes and audit/history tables.
- Data engineering: deterministic synthetic data generator with CSV/JSON exports, validation manifest and separate ground-truth labels.
- Quality: request validation, workflow policies, scheduling checks, audit trail and automated tests.

## Implemented workflows

- Department and job requisition management, including submit/approve/reject/close lifecycle.
- Candidate registration and application creation.
- Rule-based screening with mandatory gates, weighted scoring and REVIEW outcomes.
- Interview scheduling, rescheduling, cancellation, panel assignment and interviewer conflict checks.
- Structured interviewer feedback and selection only after required feedback.
- Offer creation, approval, issue and candidate accept/reject response.
- Application stage history, audit events and explicit data-issue records.

## Prerequisites

- Java 21
- PostgreSQL 14+
- Node.js 20+
- Windows PowerShell (commands below use the Maven wrapper for Windows)

## Database setup

Use the existing database **yottaflex-hackathon** on localhost port **5432**. Do not create another database.

1. Open pgAdmin and connect to PostgreSQL.
2. Select the yottaflex-hackathon database.
3. Open Query Tool and execute database/schema.sql if the schema has not already been applied.
4. Set DB_PASSWORD in your local IntelliJ Run Configuration or shell. Never commit credentials.

The application uses spring.jpa.hibernate.ddl-auto=validate; Hibernate checks mappings but does not silently create or alter the schema.

PowerShell example:

~~~powershell
$env:DB_PASSWORD = "YOUR_POSTGRES_PASSWORD"
.\mvnw.cmd spring-boot:run
~~~

## Run backend

From the repository root:

~~~powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
~~~

API base path: http://localhost:8080/api/v1

Health checks:
- http://localhost:8080/actuator/health
- http://localhost:8080/api/health

## Run frontend

In a second terminal:

~~~powershell
cd frontend
npm ci
npm run lint
npm run build
npm run dev
~~~

Vite runs on http://localhost:5173 and proxies /api requests to the backend on port 8080.

## Synthetic data

Generate the full deterministic dataset (seed defaults to 20261022):

~~~powershell
python data-generator/generate.py --seed 20261022 --profile full
~~~

Starter-sized dataset:

~~~powershell
python data-generator/generate.py --seed 20261022 --profile starter
~~~

The generator writes CSV and JSON exports, exports/manifest.json with row counts and SHA-256 checksums, and exports/ground_truth/anomalies.json. Ground-truth labels are kept separate from ordinary input tables.

To load generated records into an empty schema, run from the repository root:

~~~powershell
psql -v ON_ERROR_STOP=1 -U postgres -d yottaflex-hackathon -f database/load-exports.psql
~~~

The loader is designed for an empty schema and does not truncate existing data. Do not run it twice against an already populated database.

## Verification

~~~powershell
.\mvnw.cmd test
cd frontend
npm run lint
npm run build
~~~

The generator is deterministic: re-running it with the same seed/profile produces identical CSV SHA-256 checksums.

## Security and scope

- Synthetic candidate records only; no real candidate PII or resume files.
- Database credentials belong in environment variables, never source control.
- Actor values in requests support workflow traceability in this hackathon build; they are not authentication.
- Production authentication, authorization and real resume-file storage are not implemented. The current workspace UI is a demo workspace, not a secured multi-user identity system.
