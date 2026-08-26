# AGENTS.md

Spring Boot 4 (parent 4.1.1) / Spring Framework 6 reactive **WebFlux** failover service on
**Java 25** (enforced by the maven-enforcer plugin). Single Maven module, package
`ch.dboeckli.springframeworkguru.kbe.inventory.failover`. It exposes a functional endpoint
(`/inventory-failover`, `RouterFunction`/`Handler`) returning a static `BeerInventoryDto` list
(from `kbe-brewery-lib`) and is packaged as Docker image and Helm chart.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=InventoryHandlerTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true` /
  `-Dskip.start.stop.springboot=true`.
- Start locally: `./mvnw spring-boot:run` (app on `:8083`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`. Spotless uses shfmt `3.13.1` for shell scripts.
- Spotless excludes `AGENTS.md`/`CLAUDE.md` from flexmark (markdown) formatting.

## External dependency gotcha

- `kbe-brewery-lib` (`ch.dboeckli.springframeworkguru.kbe.lib:kbe-brewery-lib:0.0.6`) is resolved
  from GitHub Packages (`maven.pkg.github.com`). Without a PAT in `~/.m2/settings.xml`
  (server id `github`) the build cannot resolve the dependency.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- No Testcontainers / `compose.yaml` — `ActuatorInfoIT` starts the app on a random port
  (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) and checks the actuator endpoints
  (`/actuator/info`, `/actuator/health`, `/actuator/prometheus`); `InventoryHandlerTest` is a
  `@WebFluxTest` slice for the functional endpoint.

## Architecture

- `web/` contains the functional `RouterFunction` + `Handler` for `/inventory-failover`.
- Actuator is fully exposed (health/liveness/readiness probes configured in
  `src/main/resources/application.yaml`, matching the Helm `deployment.yaml` probes).

## Helm / Deploy

- Chart in `helm-charts/`, packaged to `target/helm/repo/kbe-brewery-inventory-failover-chart-<version>.tgz`,
  release name = `kbe-brewery-inventory-failover`, namespace `kbe-brewery-inventory-failover`,
  NodePort `30083`.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml` (in-cluster, pulls the chart from Docker Hub
  `oci://registry-1.docker.io/domboeckli`); `release.yml` runs `mvn release:prepare release:perform`
  on main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Dependency updates are managed via `.github/renovate.json`; validate changes with
  `renovate-config-validator`.
