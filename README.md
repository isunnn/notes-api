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

## 6. GitHub Actions (CI)
Se configuró un workflow básico para:
- Checkout del repo
- Setup de Java
- Ejecutar `./mvnw test`

Ubicación: `.github/workflows/` (workflow de CI dentro de esa carpeta).