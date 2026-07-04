#!/usr/bin/env bash
# Branding wall: the internal engine codename must be ABSENT from every customer-facing
# surface this repository AUTHORS — the scan workflow and the customer-facing README.
#
# Scope rationale (read before widening): this greps the AUTHORED TEXT surfaces, not
# compiled blobs or the internal engineering docs. A vendored/fetched scanner artifact is a
# compiled blob whose internal symbol strings may still contain the codename (only a
# hardened or Ferralon-brand build is scrubbed at rest — the binary-distribution follow-on,
# see FERRALON_SCAN.md); those symbols are never RENDERED to a customer, so they are out of
# scope here. FIRING.md is an internal engineering doc (not customer-facing), so it is out
# of scope by design and deliberately names the codename to document the fire.
set -euo pipefail
cd "$(dirname "$0")/.."

CODENAME='tegron'   # the internal engine codename — must not appear on authored surfaces
SURFACES=(
  ".github/workflows"
  "FERRALON_SCAN.md"
)
# (check_branding.sh itself is excluded — it must name the codename literal to check for
# it; it is not a customer-facing surface.)

# Allowlisted literals — STABLE CROSS-TEAM CONTRACT IDENTIFIERS that intentionally retain
# the "tegron" token even in brand-stealth builds; they are not brand surfaces:
#   - refs/tegron/state   the StateStore git ref the backend reader keys off (plumbing;
#                         pinned by Operator/Eric agreement, backend ADR-0015).
#   - tegron.report.v1    the on-disk report SCHEMA id downstream consumers key off.
ALLOW='refs/tegron/state|tegron\.report\.v1'

hits="$(grep -rinE "$CODENAME" "${SURFACES[@]}" 2>/dev/null | grep -viE "$ALLOW" || true)"

if [ -n "$hits" ]; then
  echo "BRANDING WALL VIOLATION — engine codename found on an authored customer surface:" >&2
  echo "$hits" >&2
  exit 1
fi
echo "Branding wall clean ✓ — codename '${CODENAME}' absent from authored surfaces (refs/tegron/state plumbing literal allowlisted)."
