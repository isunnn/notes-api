# DevOps Notes API (Spring Boot)

Mini proyecto backend (Java 21 + Spring Boot) para demostrar un flujo de trabajo GitFlow, uso de Pull Requests y CI con GitHub Actions.

## Stack
- Java 21
- Spring Boot
- Maven Wrapper
- H2

---

## 1. ¿Por qué GitFlow?
La utilización de*GitFlow para este encargo se justifica por el control y orden que entrega sobre el ciclo de vida del código. Al separar el trabajo en ramas `develop`, `feature/*` y `hotfix/*`, se mantiene `main` como una rama estable y, al mismo tiempo, se obtiene un historial claro de cambios y versiones mediante Pull Requests, cumpliendo con las exigencias del encargo.

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
- `hotfix: ordenar listado de notas por fechaCreacion desc`
- `release: v1.0.0 (features crear + listar notas)`
- `chore: sync main into develop (hotfix)`
- `docs: actualizar README`

### Revisión mediante Pull Requests
- Todo cambio relevante entra por PR (no commits directos a `main`)
- Los PR incluyen título, descripción y verificación de GitHub Actions

---

---

## 6. CI/CD (GitHub Actions) + Calidad + Trazabilidad 

Este repositorio implementa un pipeline CI/CD en GitHub Actions que automatiza:
- Construcción y pruebas (JUnit)
- Análisis de calidad de código (SonarCloud / SonarQubeCloud)
- Construcción de imagen Docker
- Despliegue simulado en un entorno local con Docker Compose + smoke test
- Escaneo/gestión de dependencias con Dependabot

### Workflow
Ubicación: `.github/workflows/ci.yml`

Se ejecuta en:
- `push` a `develop` y `main`
- `pull_request` hacia `develop` y `main`

### Etapas del pipeline 
1) **Tests**  
   Ejecuta:
   ```bash
   ./mvnw test
   ```
   - Incluye tests unitarios y de capa web (MockMvc).

2) **Análisis SonarCloud**  
   Ejecuta:
   ```bash
   ./mvnw sonar:sonar
   ```
   - Publica resultados en SonarCloud y aplica Quality Gate.

3) **Build de imagen Docker**  
   Ejecuta:
   ```bash
   docker build -t notes-api:<commit_sha> .
   ```

4) **Despliegue simulado con Docker Compose**  
   Ejecuta:
   ```bash
   docker compose up -d --build
   curl -f http://localhost:8080/api/notas
   docker compose down
   ```

### Dependabot 
Dependabot está configurado en:
- `.github/dependabot.yml`

Crea Pull Requests automáticos para actualizar:
- Dependencias Maven (`pom.xml`)
- Actions del workflow

### Bloqueos y calidad
Para asegurar calidad y estabilidad:
- Se configuró Branch protection en `develop` y `main` para exigir que pasen los checks:
  - `test-and-sonar`
  - `docker-compose-despliegue-simulado`
  - `SonarCloud Code Analysis`
- Si falla el análisis (tests/Sonar/smoke test), el PR no puede mergearse.

### Trazabilidad
La trazabilidad se garantiza porque:
- Todo cambio entra por Pull Request (no hay commits directos a ramas protegidas).
- Cada PR queda asociado a:
  - Commits específicos
  - Resultados del workflow 
  - Resultado del análisis SonarCloud
- La imagen Docker puede etiquetarse con el SHA del commit, permitiendo rastrear qué versión exacta se desplegó.