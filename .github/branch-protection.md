# Branch protection rules (recommended)

Use these settings on `main` and `dev`:

- Require a pull request before merging
- Require approvals: 1 (minimum)
- Dismiss stale pull request approvals when new commits are pushed
- Require status checks to pass before merging
- Require branches to be up to date before merging
- Restrict who can push to matching branches (optional, recommended for teams)

## Required checks

Add these checks as required:

- `verify`
- `Analyze (java)`

Notes:

- The `verify` check comes from `.github/workflows/ci.yml`.
- The CodeQL check name may vary by matrix language; for this repo it is `Analyze (java)`.
- `docker-build` is intentionally not required for merge by default, but can be added if container publishing is part of your release gate.
