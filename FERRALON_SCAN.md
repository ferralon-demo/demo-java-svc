# Ferralon Assay

This repository runs a **free, deterministic dependency-advisory scan** in its own
GitHub Actions runner on every push and pull request
([`.github/workflows/ferralon-assay.yml`](.github/workflows/ferralon-assay.yml)).

## What it does — and what it does not

The scan reads this module's **dependency graph** and the **public advisory feeds**, and
reports, for each advisory it evaluates, one of three honest deterministic outcomes:

| Outcome | Meaning |
|---|---|
| **Reachable candidate** | A path to the vulnerable symbol was found. A **candidate** for review — *never* a claim that it is exploitable here. |
| **Not exploitable** | The advisory applies, but the vulnerable code is provably guarded or unreachable in this codebase. |
| **Disqualified** | The advisory provably does not apply — the resolved version is outside the affected range. |

Reachability grade and EPSS/KEV are **evidence strength and in-the-wild context** — not
proofs of exploitability for this codebase. The free scan never emits an "exploitable"
verdict; that requires the consented, execution-backed **Prove** tier in the console.

**No source code leaves this repository.** The scan runs entirely in your runner. The
only thing sent off-repo is a **thin metadata allowlist** — the resolved package set
(purls), the GHSA/CVE advisories that matched, and which feed asserted them. Source
code, file contents, and developer identity are **never** transmitted (the request body
is closed to anything outside that allowlist by construction).

## What this service is

`demo-java-svc` is an ordinary Spring MVC web service, built as a WAR and deployed on
Tomcat. It exposes a tiny API:

| Route | Purpose |
|---|---|
| `GET /` | usage banner |
| `GET /healthz` | liveness probe |
| `POST /greeting` | bind form fields (`name`, `message`) onto a command object |
| `POST /echo` | round-trip a JSON body through the JSON binder |

Its externally observable behaviour is that of a normal web service; nothing user-visible
references the scanner or its brand.

## Where the results go

- **`report.json`** — the full scan result, persisted to the `refs/tegron/state` git ref
  on each default-branch (`vulnerable`) run, and uploaded as the `ferralon-scan-report`
  build artifact.
- **The pivot Issue** — a pinned Issue summarizing the run and linking to the **Ferralon
  console**, where you can see the per-advisory reasoning, the call paths behind each
  reachable candidate, and (with consent) the paid **Prove** escalation.
- **The Ferralon console** — `https://console.ferralon.com/r/demo-java-svc`.
- **The Ferralon backend** — a thin metadata allowlist is POSTed to the backend's ingest
  endpoint, authenticated by a GitHub-signed Actions OIDC token bound to this
  repository's owner. The backend binds the OIDC token to an active install (403
  otherwise) — install state, not any workflow variable, is what guards ingest.

## How the scanner is distributed (and the planned cleanup)

The in-runner scanner distribution for this repository follows the same path as the other
demo fixtures — a pinned, checksummed scanner is fetched (or vendored) at run time and
executed directly. Two follow-ons remain tracked by the Operator, not solved here:

1. **Proper distribution** — fetch a pinned, checksummed release of the scanner at run
   time rather than committing the binary into the repository.
2. **Brand-hardened build** — a hardened or Ferralon-brand build so the compiled artifact
   carries no internal engine symbols at rest. (The *authored* surfaces — this workflow,
   the pivot Issue, this README — are already codename-clean, checked in CI by
   `.ferralon/check_branding.sh`.)
