# Synthetic data generator

Run from the project root:

~~~powershell
python data-generator/generate.py --seed 20261022 --profile full
~~~

Profiles:
- full: 100 requisitions, 2,000 candidates, 3,000 applications, 600 interviews, 150 offers, plus related master/traceability records.
- starter: 10 requisitions, 100 candidates, 150 applications, 30 interviews, 8 offers.

The output includes per-table CSV and JSON, a SHA-256 manifest, and a separate ground_truth/anomalies.json. Five deliberate anomaly classes are represented: duplicate application, interviewer schedule conflict, missing feedback with advanced stage, out-of-band offer, and active application on a closed requisition.

The generator uses only Python's standard library and synthetic example.test identities. Re-running with the same seed and profile must preserve every CSV checksum.
