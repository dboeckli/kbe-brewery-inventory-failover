# kbe-brewery-inventory-failover

Spring Boot 4 (4.1.1) / Spring Framework 6 reactive **WebFlux** failover service for the brewery
inventory microservices (KBE) on **Java 25**. It exposes a functional endpoint
(`/inventory-failover`, `RouterFunction`/`Handler`) that returns a static `BeerInventoryDto` list
(from `kbe-brewery-lib`) and is packaged as a Docker image and a Helm chart.

Original git repository: https://github.com/springframeworkguru/kbe-sb-microservices.git

## Build

```bash
./mvnw clean verify
```

- Runs unit (`*Test`) + IT (`*IT`) tests, Helm lint/template and format checks
  (spring-javaformat + spotless).
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/`. Skip the Docker build with `-Dskip.docker.build=true` /
  `-Dskip.start.stop.springboot=true`.
- Start locally: `./mvnw spring-boot:run` (app on `:8083`).

## Deployment with Helm

The chart is deployed into the namespace `kbe-brewery-inventory-failover` and exposed via
NodePort `30083`.

Build and package the chart:

```bash
./mvnw clean install -Dskip.start.stop.springboot=true -Dskip.docker.build=true
```

Go to the directory where the tgz file has been created:

```powershell
cd target/helm/repo
```

Unpack:

```powershell
$file = Get-ChildItem -Filter kbe-brewery-inventory-failover-chart-*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

Install:

```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace kbe-brewery-inventory-failover --create-namespace --wait --timeout 8m --debug --render-subchart-notes
```

Show logs (replace `$POD` with the pods from the command below):

```powershell
kubectl get pods -l app.kubernetes.io/name=kbe-brewery-inventory-failover -n kbe-brewery-inventory-failover
kubectl logs $POD -n kbe-brewery-inventory-failover --all-containers
```

Test:

```powershell
helm test $APPLICATION_NAME --namespace kbe-brewery-inventory-failover --logs
```

Uninstall:

```powershell
helm uninstall $APPLICATION_NAME --namespace kbe-brewery-inventory-failover
```

Delete all:

```powershell
kubectl delete all --all -n kbe-brewery-inventory-failover
```

Create a busybox sidecar:

```powershell
kubectl run busybox-test --rm -it --image=busybox:1.37.0 --namespace=kbe-brewery-inventory-failover --command -- sh
```

Verify the actuator and the endpoint via NodePort `30083`:

```bash
curl http://localhost:30083/actuator/health
curl http://localhost:30083/inventory-failover
```

The IntelliJ run configurations in `.run/` (`deploy-k8s`, `test-k8s`, `uninstall-k8s`,
`clear-docker`) wrap the scripts in `.run/scripts/` for the same workflow.

## Sandbox (local dev environment)

The sandbox is provisioned by the opencode-sandbox-kit and runs as a Docker container. It mounts this
repo, starts opencode, and connects the IntelliJ MCP server. The app runs standalone (no upstream
services, no `compose.yaml`) on port 8083.

Allow the kit source (GitHub without cloning):

```powershell
sbx settings set kit.allowedSources --% "[\"docker.io/\",\"github.com/dboeckli/\"]"
```

Start a new sandbox:

```powershell
sbx run opencode --name kbe-brewery-inventory-failover --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" "C:\development\projects\kbe-brewery-inventory-failover"
```

Start the sandbox with Kubernetes support:

```powershell
sbx run opencode --name kbe-brewery-inventory-failover --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" "C:\development\projects\kbe-brewery-inventory-failover" "$env:USERPROFILE\.kube:ro"
```

Apply the kit to an existing sandbox (restarts the sandbox, VM state is kept):

```powershell
sbx kit add kbe-brewery-inventory-failover "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent"
```

## Contributing

Contributions to improve this template are welcome. Please follow the standard GitHub flow:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request
