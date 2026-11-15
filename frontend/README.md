# Frontend structure and standards

```text
src/
  app/                 # Application shell, navigation and layout
  features/
    dashboard/         # Dashboard page and view styles
    requisitions/      # Requisition workflow
    candidates/        # Candidate management
    applications/      # Application and screening workflow
    interviews/        # Scheduling, panel and feedback workflow
    offers/            # Offer lifecycle
    audit/             # Read-only audit history viewer
  shared/
    api/               # Shared API client and its unit tests
  assets/              # Static assets
  index.css            # Global design tokens and base styles
  main.jsx             # React entry point
```

## Conventions

- Keep workflow UI inside its feature folder; share transport logic through `shared/api`.
- Keep API calls in the shared client so JSON handling and error messages remain consistent.
- Use controlled form fields, browser constraints and server-side validation; never rely on frontend validation alone.
- Show loading, empty and error states for asynchronous requests.
- Use semantic labels, buttons, navigation landmarks, and `role=alert` / `role=status` for feedback.
- Keep derived UI values derived from state; do not duplicate server state unnecessarily.
- Keep credentials out of frontend source. The Vite proxy targets the local backend only in development.

## Commands

```powershell
npm ci
npm run lint
npm test
npm run build
```

The API helper tests use Node's built-in test runner and require no extra test framework.
