# DevOps Notes API (Spring Boot)

Mini proyecto backend (Java 21 + Spring Boot) para demostrar un flujo de trabajo GitFlow, uso de Pull Requests y CI con GitHub Actions.

## Stack
- Java 21
- Spring Boot
- Maven Wrapper
- H2

---

## 1. ¿Por qué GitFlow?
La utilización de GitFlow para este encargo se justifica por el control y orden que entrega sobre el ciclo de vida del código. Al separar el trabajo en ramas `develop`, `feature/*` y `hotfix/*`, se mantiene `main` como una rama estable y, al mismo tiempo, se obtiene un historial claro de cambios y versiones mediante Pull Requests, cumpliendo con las exigencias del encargo.

## Branches
- `main`: versión estable
- `develop`: integración/desarrollo (donde se integran features antes de liberar)
- `feature/*`: desarrollo de funcionalidades (se crean desde `develop` y vuelven a `develop` por PR)
- `hotfix/*`: corrección de errores urgentes en producción (se crean desde `main`, vuelven a `main` por PR y luego se sincronizan a `develop`)

---

## 2. Cómo ejecutar el proyecto

### Ejecutar
```bash
./mvnw spring-boot:run
```

### Ejecutar tests
```bash
./mvnw test
```

---

## 3. Endpoints

Base URL: `http://localhost:8080`

### Feature 1: Crear nota
- **POST** `/api/notas`

Body (JSON):
```json
{
  "nombreNota": "Crear proyecto DevOps"
}
```

Respuesta esperada: `201 Created` con la nota creada (incluye `id` y `fechaCreacion`).

### Feature 2: Listar notas
- **GET** `/api/notas`

Respuesta esperada: lista JSON de notas.

---

## 4. Simulación de trabajo colaborativo (PRs)

### Pull Requests de Features (a `develop`)
- Feature 1: `feature/crear-nota` a `develop`
  - PR: [#1](https://github.com/NicolasJimenez111/notes-api/pull/1)
- Feature 2: `feature/listar-notas` a `develop`
  - PR: [#2](https://github.com/NicolasJimenez111/notes-api/pull/2)

### Release a producción (a `main`)
- Release: `develop` (`release/v1.0.0`) a `main`
  - PR: [#3](https://github.com/NicolasJimenez111/notes-api/pull/3)

### Hotfix en producción (a `main`)
**Problema (bug):** el listado de notas no tenía un orden garantizado (dependía del motor o de la consulta).  
**Solución:** ordenar explícitamente por `fechaCreacion` descendente.

- Hotfix: `hotfix/ordenar-listado-notas` a `main`
  - PR: [#4](https://github.com/NicolasJimenez111/notes-api/pull/4)

### Sincronización del hotfix hacia `develop`
- Sync: `main` a `develop` (por PR usando rama `chore/sync-main-to-develop`)
  - PR: [#5](https://github.com/NicolasJimenez111/notes-api/pull/5)

---

## 5. Convenciones (naming, commits, PRs y revisión)

### Naming de ramas
Se utilizaron nombres convencionales sugeridos por la documentación, junto a descripciones cortas según la implementación.
- `feature/<descripcion-corta>` (ej: `feature/listar-notas`)
- `hotfix/<descripcion-corta>` (ej: `hotfix/ordenar-listado-notas`)
- `release/<version>` (ej: `release/v1.0.0`)
- `chore/<tarea>` (ej: `chore/sync-main-to-develop`)

### Convención de commits
Se utilizaron nombres descriptivos con etiquetas según el tipo de cambio.
- `feature: ...` para features
- `hotfix: ...` para correcciones en `main`
- `release: ...` para liberaciones
- `docs: ...` para documentación
- `chore: ...` para tareas de mantenimiento

Ejemplos:
- `feature: crear nota (POST /api/notas)`
- `feature: listar notas (GET /api/notas)`
- `hotfix: ordenar listado de notas`
- `release: v1.0.0 (features crear + listar notas)`
- `chore: sincronizar main a develop (hotfix)`
- `docs: actualizar README`

### Revisión mediante Pull Requests
- Todo cambio relevante entra por PR (no commits directos a `main`)
- Los PR incluyen título, descripción y verificación de GitHub Actions

---

## 6. CI/CD (GitHub Actions) + Calidad + Trazabilidad

Este repositorio implementa un pipeline CI/CD en GitHub Actions que automatiza:
- Construcción y pruebas (JUnit)
- Análisis de calidad de código (SonarCloud)
- Linting de Dockerfile (Hadolint)
- Auditoría de seguridad y configuración (script custom)
- Despliegue en Amazon EKS (Kubernetes) + Istio + pruebas de aceptación
- Monitoreo (Prometheus + Grafana)
- Escaneo/gestión de dependencias (Dependabot)

### Workflow
Ubicación: `.github/workflows/ci.yml`

Se ejecuta en:
- `push` a `develop` y `main`
- `pull_request` hacia `develop` y `main`

### Jobs del pipeline

#### Job 1: `test-and-sonar`
Ejecuta tests unitarios y análisis de calidad:
```bash
./mvnw -B test
./mvnw -B sonar:sonar
```
- Tests JUnit (unitarios + MockMvc)
- Análisis SonarCloud (code smells, bugs, vulnerabilidades)
- Verificación de Quality Gate (el PR se bloquea si no pasa)

#### Job 2: `lint-dockerfile`
Linting del Dockerfile con Hadolint:
```yaml
- uses: hadolint/hadolint-action@v3.1.0
  with:
    dockerfile: dockerfile
    failure-threshold: warning
```
- Detecta prácticas inseguras en el Dockerfile
- Bloquea si encuentra warnings o errores

#### Job 3: `audit`
Auditoría de seguridad y configuración:
```bash
chmod +x scripts/audit.sh && ./scripts/audit.sh
```
El script verifica:
- Presencia de secretos hardcodeados (passwords, tokens, keys)
- Uso de root user en Dockerfile
- Archivos `.env` committeados
- Uso de tags `:latest` en imágenes
- Permisos de archivos sensibles

#### Job 4: `deploy-eks`
Despliegue en Amazon EKS con service mesh Istio:
```bash
# Login a ECR y build de imagen Docker
aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
docker build -t $ECR_REGISTRY/notes-api:$IMAGE_TAG .
docker push $ECR_REGISTRY/notes-api:$IMAGE_TAG

# Configurar kubectl y desplegar
aws eks update-kubeconfig --name notes-api-cluster --region us-east-1
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml

# Instalar Istio y configurar service mesh
istioctl install --set profile=demo -y
kubectl apply -f k8s/istio/gateway.yaml
kubectl apply -f k8s/istio/virtualservice.yaml
kubectl apply -f k8s/istio/destinationrule.yaml

# Pruebas de aceptación
kubectl port-forward -n notes-api svc/notes-api 8080:80 &
./scripts/acceptance-test.sh http://localhost:8080
```
- Solo se ejecuta si los 3 jobs anteriores pasan y es push a `main` o `develop`
- Login a Amazon ECR, build y push de imagen Docker etiquetada con SHA del commit
- Configura kubectl contra el clúster EKS
- Despliega namespace, deployment, service y HPA
- Instala Istio (service mesh) con profile `demo`
- Configura Gateway, VirtualService y DestinationRule para routing de tráfico
- Habilita sidecar injection (envoy proxy) en el namespace
- Ejecuta pruebas de aceptación (health check, GET, POST, verify)
- En caso de fallo, realiza rollback automático (`rollout undo`)

### Dependabot
Dependabot está configurado en `.github/dependabot.yml`:
- Dependencias Maven (`pom.xml`): revisión semanal
- Actions del workflow: revisión semanal

### Bloqueos y calidad
Branch protection en `develop` y `main` exige que pasen:
- `test-and-sonar` (tests + Quality Gate)
- `lint-dockerfile` (Hadolint)
- `audit` (auditoría de seguridad)
- `deploy-eks` (despliegue exitoso en EKS + Istio + pruebas de aceptación)

Si falla cualquiera de estos jobs, el PR no puede mergearse.

### Trazabilidad
- Todo cambio entra por Pull Request
- Cada PR queda asociado a commits, resultados del workflow y SonarCloud
- La imagen Docker se etiqueta con el SHA del commit
- Kubernetes mantiene historial de revisiones (`kubectl rollout history`)

---

## 7. Monitoreo (Prometheus + Grafana)

### Métricas monitoreadas
- **Requests por minuto** (rate de HTTP requests)
- **Latencia P95** (tiempo de respuesta)
- **Errores 5xx y 4xx** (tasa de errores)
- **CPU y memoria** (uso de recursos)
- **JVM Heap Memory** (uso de memoria Java)
- **Uptime** (tiempo de actividad)
- **Test Coverage** (cobertura de código desde SonarCloud)

### Dashboard de Grafana
Ubicación: `grafana/dashboards/notes-api.json`
- 9 paneles preconfigurados
- Se carga automáticamente al iniciar Grafana (provisioning)
- Datasource: Prometheus (configurado en `grafana/provisioning/`)

### Cómo ejecutar localmente
```bash
docker compose up -d
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/admin)
```

---

## 8. Cumplimiento y seguridad

### Hadolint
Linting estático del Dockerfile. Verifica:
- No usar `latest` como tag de imagen base
- No ejecutar como root
- Copiar dependencias antes del código fuente
- Usar `COPY` en vez de `ADD`
- Configurar `HEALTHCHECK`

### audit.sh (script custom)
Script de auditoría que verifica:
- **Secretos**: busca patrones como `password=`, `token=`, `secret=`, `key=`
- **Root user**: verifica que el Dockerfile no ejecute como root
- **Archivos .env**: detecta si hay archivos `.env` committeados
- **Latest tags**: verifica que las imágenes no usen `:latest`
- **Permisos**: revisa permisos de archivos sensibles

### Quality Gate de SonarCloud
El pipeline verifica que el código cumple estándares de calidad:
- Cobertura de código mínima
- Número máximo de bugs aceptables
- Número máximo de code smells aceptables
- Sin vulnerabilidades críticas

Si el Quality Gate falla, el PR se bloquea automáticamente.

---

## 9. Despliegue Kubernetes (Amazon EKS)

### Cluster EKS
- **Nombre**: `notes-api-cluster`
- **Región**: `us-east-1`
- **Kubernetes**: v1.36
- **Nodos**: 2 (tipo `t3.medium`)
- **Proveedor**: AWS Academy Learner Lab

### Service Mesh (Istio)
- **Profile**: `demo` 
- **Sidecar injection**: habilitado en namespace `notes-api`
- **Componentes desplegados**:
  - `istiod` 
  - `istio-ingressgateway` 
  - `istio-egressgateway` 
- **Configuración**:
  - `Gateway`: expone puerto 80 para tráfico HTTP
  - `VirtualService`: enruta tráfico al servicio `notes-api:80`
  - `DestinationRule`: define load balancing (ROUND_ROBIN) y connection pooling

### Deployment (detalles)
- **Réplicas**: 2 (alta disponibilidad)
- **imagePullPolicy**: Always (desde Amazon ECR)
- **Imagen**: `056172563599.dkr.ecr.us-east-1.amazonaws.com/notes-api`
- **Readiness Probe**: `/actuator/health`
- **Liveness Probe**: `/actuator/health`
- **Resources**:
  - Requests: 256Mi RAM, 250m CPU
  - Limits: 512Mi RAM, 500m CPU

### HPA (Horizontal Pod Autoscaler)
- **Mínimo**: 2 réplicas
- **Máximo**: 5 réplicas
- **Métricas**:
  - CPU: escala si supera 70% de uso
  - Memory: escala si supera 80% de uso

### Pruebas de aceptación
El pipeline ejecuta automáticamente 4 pruebas contra la API desplegada:
1. **Health check**: verifica que `/actuator/health` retorna 200
2. **GET /api/notas**: verifica que el listado retorna 200
3. **POST /api/notas**: verifica que la creación retorna 201
4. **Verificar nota**: confirma que la nota creada aparece en el listado

---

## 10. Decisiones técnicas 

### Integración en el pipeline CI/CD

Cada herramienta se ejecuta en una etapa específica del pipeline y bloquea el avance si falla:

- **SonarCloud + Quality Gate**: se ejecutan en el job `test-and-sonar`. Analizan calidad del código y verifican que cumpla umbrales mínimos. Si el Quality Gate falla, el pipeline se detiene.
- **Hadolint**: se ejecuta en el job `lint-dockerfile`. Valida el Dockerfile contra mejores prácticas. Si detecta warnings, el pipeline se detiene.
- **audit.sh**: se ejecuta en el job `audit`. Busca secretos, root user, archivos `.env` y tags `:latest`. Si encuentra algo, el pipeline se detiene.
- **Dependabot**: crea PRs automáticos para actualizar dependencias Maven y Actions de GitHub.
- **Kubernetes + HPA + Probes**: se despliegan en el job `deploy-eks` solo si los 3 jobs anteriores pasaron. HPA ajusta réplicas según CPU/memoria. Probes reinician pods que dejan de responder.
- **Istio**: se instala como service mesh en el job `deploy-eks`. Proporciona traffic management, observabilidad y seguridad en malla (mTLS entre pods).
- **Pruebas de aceptación**: se ejecutan en el job `deploy-eks` después del despliegue. Validan que la API funcione correctamente en el clúster EKS.
- **Prometheus + Grafana**: monitorean la aplicación después del despliegue, exponiendo métricas de rendimiento y calidad.

### Decisiones técnicas justificadas

- **GitFlow**: control de versiones con hotfixes paralelos y releases gestionados por PR.
- **SonarCloud + Quality Gate**: aseguran que solo código sin bugs críticos ni vulnerabilidades llegue a producción.
- **Hadolint + audit.sh**: refuerzan la seguridad de la imagen Docker y del repositorio.
- **Kubernetes (EKS)**: brinda alta disponibilidad, self-healing y escalamiento automático en un entorno-managed por AWS.
- **Istio**: aporta traffic management (routing, retries, timeouts), observabilidad (métricas, tracing) y seguridad (mTLS) sin cambiar código de la aplicación.
- **Prometheus + Grafana**: permiten detectar anomalías en tiempo real y tomar decisiones operacionales basadas en datos.
