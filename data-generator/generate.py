#!/usr/bin/env python3
"""Deterministic synthetic recruitment data generator.

Usage:
  python data-generator/generate.py
  python data-generator/generate.py --seed 20261022 --profile starter
  python data-generator/generate.py --seed 20261022 --output exports

Synthetic data only. Ground-truth anomaly labels are written separately.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import random
from datetime import date, datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NOW = datetime(2026, 11, 15, 12, 0, tzinfo=timezone.utc)
FIRST = ["Aarav", "Ananya", "Ishaan", "Meera", "Kabir", "Diya", "Arjun", "Priya", "Rohan", "Aisha", "Dev", "Nisha"]
LAST = ["Sharma", "Reddy", "Kumar", "Patel", "Iyer", "Rao", "Mehta", "Singh", "Nair", "Das", "Gupta", "Joshi"]
CITIES = ["Hyderabad", "Bengaluru", "Pune", "Chennai", "Mumbai", "Delhi NCR", "Remote - India"]
SKILLS = ["Java", "Spring Boot", "SQL", "PostgreSQL", "React", "JavaScript", "Python", "Docker", "AWS", "REST APIs",
          "Hibernate", "Microservices", "Kafka", "Redis", "Git", "Linux", "JUnit", "TypeScript", "LLM", "RAG"]
DEPARTMENTS = ["Engineering", "Product", "Data & Analytics", "Platform", "Security", "Operations", "Finance", "People", "Design", "Customer Success"]
TITLES = ["Java Backend Engineer", "Full Stack Developer", "Software Engineer", "Data Analyst", "Platform Engineer",
          "Frontend Developer", "QA Automation Engineer", "DevOps Engineer", "AI Application Engineer", "Product Analyst"]
ENUMS = {
    "employment_type": ["FULL_TIME", "CONTRACT", "INTERN"],
    "application_stage": ["APPLIED", "SCREENING", "SHORTLISTED", "INTERVIEW", "SELECTED", "REJECTED", "WITHDRAWN", "OFFERED", "HIRED"],
    "interview_type": ["TECHNICAL", "MANAGERIAL", "HR", "FINAL"],
    "interview_status": ["SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW"],
    "offer_status": ["DRAFT", "PENDING_APPROVAL", "APPROVED", "ISSUED", "ACCEPTED", "REJECTED", "EXPIRED"],
}

def iso(dt: datetime) -> str:
    return dt.isoformat(timespec="seconds")

def stamp(rng: random.Random, days_back: int = 300) -> str:
    return iso(NOW - timedelta(days=rng.randint(0, days_back), hours=rng.randint(0, 23)))

def write_table(base: Path, name: str, rows: list[dict]) -> tuple[int, str]:
    base.mkdir(parents=True, exist_ok=True)
    if not rows:
        return 0, hashlib.sha256(b"").hexdigest()
    columns = list(rows[0].keys())
    csv_path = base / "csv" / f"{name}.csv"
    json_path = base / "json" / f"{name}.json"
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    json_path.parent.mkdir(parents=True, exist_ok=True)
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)
    json_path.write_text(json.dumps(rows, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    digest = hashlib.sha256(csv_path.read_bytes()).hexdigest()
    return len(rows), digest

def generate(seed: int, profile: str, output: Path) -> dict:
    rng = random.Random(seed)
    sizes = {"starter": (10, 100, 150, 30, 8), "full": (100, 2000, 3000, 600, 150)}
    req_n, cand_n, app_n, interview_n, offer_n = sizes[profile]
    departments = [{"department_id": f"DEP-{i+1:03d}", "name": name, "cost_center": f"CC-{i+1:03d}", "created_at": stamp(rng)}
                   for i, name in enumerate(DEPARTMENTS[:min(req_n, len(DEPARTMENTS))])]
    requisitions = []
    for i in range(req_n):
        dep = departments[i % len(departments)]
        low = rng.randrange(450000, 1000000, 25000)
        high = low + rng.randrange(200000, 800001, 25000)
        status = "CLOSED" if i == 0 else "APPROVED"
        created = NOW - timedelta(days=rng.randint(30, 240))
        requisitions.append({
            "requisition_id": f"REQ-2026-{i+1:06d}", "department_id": dep["department_id"],
            "title": rng.choice(TITLES), "description": "Synthetic role profile. Evaluate only job-related skills and experience.",
            "employment_type": rng.choice(ENUMS["employment_type"]), "location": rng.choice(CITIES),
            "openings": rng.randint(1, 5), "salary_min": low, "salary_max": high, "currency": "INR",
            "status": status, "requested_by": f"requester{i%17+1}@example.test",
            "approved_by": f"approver{i%9+1}@example.test", "approved_at": iso(created + timedelta(days=1)),
            "opened_at": iso(created + timedelta(days=2)), "closed_at": iso(created + timedelta(days=30)) if status == "CLOSED" else "",
            "created_at": iso(created), "updated_at": iso(created + timedelta(days=2))
        })
    candidates = []
    for i in range(cand_n):
        candidates.append({
            "candidate_id": f"CAND-{i+1:06d}", "full_name": f"{rng.choice(FIRST)} {rng.choice(LAST)}",
            "email": f"candidate{i+1:06d}@example.test", "phone": f"+91-90000{i%100000:05d}",
            "current_location": rng.choice(CITIES), "total_experience_months": rng.randint(0, 120), "created_at": stamp(rng)
        })
    skills = [{"skill_id": f"SKILL-{i+1:03d}", "name": name, "category": "TECHNICAL"} for i, name in enumerate(SKILLS)]
    candidate_skills = []
    for candidate in candidates:
        selected = rng.sample(skills, k=rng.randint(3, min(8, len(skills))))
        for skill in selected:
            candidate_skills.append({
                "candidate_id": candidate["candidate_id"], "skill_id": skill["skill_id"],
                "proficiency": rng.choice(["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"]),
                "years_experience": round(rng.uniform(0, 7), 1), "evidence_source": rng.choice(["RESUME", "ASSESSMENT", "DECLARED"]),
                "verified": rng.choice([True, False])
            })
    screening_rules = []
    for req in requisitions:
        for j, skill in enumerate(rng.sample(skills, 3)):
            screening_rules.append({
                "rule_id": f"RULE-{req['requisition_id']}-{j+1}", "requisition_id": req["requisition_id"],
                "skill_id": skill["skill_id"], "rule_type": "REQUIRED_SKILL" if j == 0 else "PREFERRED_SKILL",
                "weight": 50 if j == 0 else 25, "threshold": 1 if j == 0 else "",
                "is_mandatory": j == 0, "effective_from": "2026-01-01", "effective_to": ""
            })
    applications = []
    app_pairs = set()
    for i in range(app_n):
        req = requisitions[i % len(requisitions)]
        candidate = candidates[(i * 7) % len(candidates)]
        if i == 0:
            candidate = candidates[1]
        if i == 1:
            req = requisitions[0]
            candidate = candidates[0]
        if i == 2:
            req = requisitions[0]
            candidate = candidates[0]  # Deliberate duplicate application pair.
        stage = rng.choice(ENUMS["application_stage"])
        if i == 0:
            stage = "APPLIED"  # Active application on the closed requisition.
        applications.append({
            "application_id": f"APP-{req['requisition_id']}-{candidate['candidate_id']}-{i+1:04d}",
            "requisition_id": req["requisition_id"], "candidate_id": candidate["candidate_id"],
            "resume_id": f"RES-{i+1:06d}", "stage": stage,
            "screening_score": round(rng.uniform(35, 99), 2), "screening_decision": rng.choice(["PASS", "FAIL", "REVIEW"]),
            "applied_at": stamp(rng, 180), "last_stage_changed_at": stamp(rng, 90)
        })
        app_pairs.add((req["requisition_id"], candidate["candidate_id"]))
    resumes = [{
        "resume_id": app["resume_id"], "candidate_id": app["candidate_id"], "application_id": app["application_id"],
        "file_name": f"{app['candidate_id']}-resume.pdf", "storage_uri": f"synthetic://resumes/{app['resume_id']}",
        "mime_type": "application/pdf", "checksum_sha256": hashlib.sha256(app["resume_id"].encode()).hexdigest(),
        "parsed_at": stamp(rng), "uploaded_at": app["applied_at"]
    } for app in applications]
    interviewers = [{
        "interviewer_id": f"INTV-{i+1:04d}", "full_name": f"{rng.choice(FIRST)} {rng.choice(LAST)}",
        "email": f"interviewer{i+1:04d}@example.test", "department_id": departments[i % len(departments)]["department_id"],
        "active": True, "timezone": "Asia/Kolkata"
    } for i in range(max(12, min(60, interview_n // 5)))]
    interviews, panels, feedback = [], [], []
    completed_apps = []
    for i in range(interview_n):
        app = applications[(i * 3 + 4) % len(applications)]
        start = NOW - timedelta(days=rng.randint(1, 45), hours=rng.randint(0, 8))
        end = start + timedelta(minutes=60)
        status = "COMPLETED" if i % 4 else "SCHEDULED"
        if i == 0:
            status = "COMPLETED"  # Feedback anomaly is kept separate in ground truth.
            app["stage"] = "SELECTED"
        iid = f"INT-2026-{i+1:06d}"
        interviews.append({
            "interview_id": iid, "application_id": app["application_id"], "interview_type": rng.choice(ENUMS["interview_type"]),
            "scheduled_start": iso(start), "scheduled_end": iso(end), "mode": rng.choice(["ONLINE", "ONSITE", "PHONE"]),
            "meeting_location": "Synthetic meeting room" if i % 3 == 0 else "", "status": status, "created_at": iso(start - timedelta(days=2))
        })
        assigned = [interviewers[(i * 2) % len(interviewers)], interviewers[(i * 2 + 1) % len(interviewers)]]
        for j, person in enumerate(assigned):
            panels.append({"interview_id": iid, "interviewer_id": person["interviewer_id"], "role": "LEAD" if j == 0 else "PANELIST"})
            if status == "COMPLETED" and not (i == 0 and j == 1):
                feedback.append({
                    "feedback_id": f"FB-{iid}-{person['interviewer_id']}", "interview_id": iid,
                    "interviewer_id": person["interviewer_id"], "score": round(rng.uniform(4, 10), 1),
                    "recommendation": rng.choice(["STRONG_YES", "YES", "HOLD", "NO"]),
                    "strengths": "Clear job-related problem solving and communication.", "concerns": "",
                    "submitted_at": iso(end + timedelta(hours=3))
                })
        if status == "COMPLETED":
            completed_apps.append(app["application_id"])
    # Force a reproducible interviewer overlap: same lead interviewer, overlapping intervals.
    if len(interviews) > 1:
        first_person = panels[0]["interviewer_id"]
        panels[2]["interviewer_id"] = first_person
        interviews[1]["scheduled_start"] = interviews[0]["scheduled_start"]
        interviews[1]["scheduled_end"] = interviews[0]["scheduled_end"]
    offers = []
    eligible = [a for a in applications if a["stage"] in ("SELECTED", "OFFERED", "HIRED")]
    while len(eligible) < offer_n:
        eligible.append(applications[len(eligible) % len(applications)])
    for i in range(offer_n):
        app = eligible[i]
        req = next(r for r in requisitions if r["requisition_id"] == app["requisition_id"])
        salary = round(rng.uniform(req["salary_min"], req["salary_max"]), 2)
        if i == 0:
            salary = req["salary_max"] + 150000  # Deliberate out-of-band offer.
        status = ["PENDING_APPROVAL", "ISSUED", "ACCEPTED", "REJECTED", "DRAFT"][i % 5]
        approved = status in ("ISSUED", "ACCEPTED", "REJECTED")
        offers.append({
            "offer_id": f"OFF-2026-{i+1:06d}", "application_id": app["application_id"], "salary": salary,
            "currency": "INR", "status": status, "approved_by": f"approver{i%9+1}@example.test" if approved else "",
            "approved_at": stamp(rng) if approved else "", "issued_at": stamp(rng) if status in ("ISSUED", "ACCEPTED", "REJECTED") else "",
            "responded_at": stamp(rng) if status in ("ACCEPTED", "REJECTED") else "",
            "valid_until": (date(2026, 12, 1) + timedelta(days=i % 20)).isoformat(), "created_at": stamp(rng)
        })
    stage_history = [{
        "application_id": app["application_id"], "from_stage": "", "to_stage": app["stage"],
        "changed_by": "synthetic-generator", "reason": "Initial synthetic application record.", "changed_at": app["applied_at"]
    } for app in applications]
    audit_events = [{
        "aggregate_type": "APPLICATION", "aggregate_id": app["application_id"], "event_type": "APPLICATION_CREATED",
        "actor": "synthetic-generator", "event_payload": json.dumps({"stage": app["stage"]}), "occurred_at": app["applied_at"]
    } for app in applications]
    truth = [
        {"label": "DUPLICATE_APPLICATION", "entity_type": "APPLICATION", "entity_id": applications[2]["application_id"], "expected_decision": "FLAG_DUPLICATE", "details": {"matching_application_id": applications[1]["application_id"]}},
        {"label": "INTERVIEWER_SCHEDULE_CONFLICT", "entity_type": "INTERVIEW", "entity_id": interviews[1]["interview_id"], "expected_decision": "BLOCK_SCHEDULE", "details": {"conflicting_interview_id": interviews[0]["interview_id"]}},
        {"label": "MISSING_FEEDBACK_STAGE_ADVANCED", "entity_type": "INTERVIEW", "entity_id": interviews[0]["interview_id"], "expected_decision": "BLOCK_SELECTION", "details": {"missing_panelist_feedback": True}},
        {"label": "OFFER_OUTSIDE_APPROVED_BAND", "entity_type": "OFFER", "entity_id": offers[0]["offer_id"], "expected_decision": "BLOCK_APPROVAL", "details": {"salary": offers[0]["salary"], "salary_max": next(r for r in requisitions if r["requisition_id"] == applications[0]["requisition_id"])["salary_max"]}},
        {"label": "CLOSED_REQUISITION_ACTIVE_APPLICATION", "entity_type": "APPLICATION", "entity_id": applications[0]["application_id"], "expected_decision": "FLAG_ACTIVE_ON_CLOSED_ROLE", "details": {"requisition_id": applications[0]["requisition_id"]}}
    ]
    issues = [{
        "issue_type": item["label"], "entity_type": item["entity_type"], "entity_id": item["entity_id"],
        "ground_truth_label": item["label"], "expected_decision": item["expected_decision"],
        "details": json.dumps(item["details"]), "detected_at": iso(NOW), "resolved_at": ""
    } for item in truth]
    tables = {
        "department": departments, "job_requisition": requisitions, "candidate": candidates, "skill": skills,
        "candidate_skill": candidate_skills, "screening_rule": screening_rules, "resume_artifact": resumes,
        "application": applications, "interviewer": interviewers, "interview": interviews,
        "interview_interviewer": panels, "feedback": feedback, "offer": offers,
        "application_stage_history": stage_history, "audit_event": audit_events, "data_issue": issues
    }
    # Referential and uniqueness checks on all generated source records.
    req_ids = {r["requisition_id"] for r in requisitions}
    cand_ids = {c["candidate_id"] for c in candidates}
    app_ids = {a["application_id"] for a in applications}
    int_ids = {i["interview_id"] for i in interviews}
    assert all(a["requisition_id"] in req_ids and a["candidate_id"] in cand_ids for a in applications)
    assert all(i["application_id"] in app_ids for i in interviews)
    assert all(p["interview_id"] in int_ids and p["interviewer_id"] in {x["interviewer_id"] for x in interviewers} for p in panels)
    assert all(o["application_id"] in app_ids for o in offers)
    assert len(app_ids) == len(applications)
    assert applications[1]["requisition_id"] == applications[2]["requisition_id"] and applications[1]["candidate_id"] == applications[2]["candidate_id"]
    assert requisitions[0]["status"] == "CLOSED" and applications[0]["stage"] not in ("HIRED", "REJECTED", "WITHDRAWN")
    assert interviews[0]["scheduled_start"] == interviews[1]["scheduled_start"]
    assert panels[0]["interviewer_id"] == panels[2]["interviewer_id"]
    assert offers[0]["salary"] > next(r for r in requisitions if r["requisition_id"] == applications[0]["requisition_id"])["salary_max"]
    output.mkdir(parents=True, exist_ok=True)
    manifest = {}
    for name, rows in tables.items():
        count, digest = write_table(output, name, rows)
        manifest[name] = {"rows": count, "csv_sha256": digest}
    gt_dir = output / "ground_truth"
    gt_dir.mkdir(parents=True, exist_ok=True)
    (gt_dir / "anomalies.json").write_text(json.dumps(truth, indent=2) + "\n", encoding="utf-8")
    manifest_doc = {"seed": seed, "profile": profile, "generated_at": "fixed-by-design", "tables": manifest,
                    "validation": {"foreign_keys": "passed", "application_primary_keys": "unique", "ground_truth_cases": len(truth)}}
    (output / "manifest.json").write_text(json.dumps(manifest_doc, indent=2) + "\n", encoding="utf-8")
    return manifest_doc

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--seed", type=int, default=20261022)
    parser.add_argument("--profile", choices=("starter", "full"), default="full")
    parser.add_argument("--output", type=Path, default=ROOT / "exports")
    args = parser.parse_args()
    report = generate(args.seed, args.profile, args.output.resolve())
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    main()



