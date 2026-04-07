# Spring Boot JWT Starter

[![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml)

Starter base para autenticacion JWT usando Spring Boot, Spring Security y JPA.

## Stack

- Java 21
- Spring Boot 4.0.3
- Spring Security
- Spring Data JPA
- PostgreSQL (runtime)
- JJWT 0.12.6
- SpringDoc OpenAPI (Swagger UI)
- H2 (solo para tests)

## Estructura base

```text
src/main/java/com/template/jwtstarter
  auth/
  security/
  user/
  common/
```

## Variables de entorno requeridas

Configura estas variables para ejecutar la app en local (perfil `dev`):

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`

Tambien puedes crear un archivo `.env` en la raiz del proyecto (ya esta habilitado en `application.yml`).
Puedes partir de `.env.example` o `.env.template` y copiarlo como `.env`.

Ejemplo (PowerShell):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/jwtstarter"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="change-this-secret-key-at-least-32-characters"
$env:JWT_REFRESH_SECRET="change-this-refresh-secret-key-at-least-32-chars"
```

Ejemplo de `.env`:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/jwtstarter
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-secret-key-at-least-32-characters
JWT_REFRESH_SECRET=change-this-refresh-secret-key-at-least-32-chars
```

## CORS

La configuracion CORS es global y se aplica desde `SecurityConfig`.

Variables opcionales:

- `CORS_ALLOWED_ORIGINS` (por defecto: `http://localhost:3000,http://localhost:5173`)
- `CORS_ALLOWED_METHODS` (por defecto: `GET,POST,PUT,PATCH,DELETE,OPTIONS`)
- `CORS_ALLOWED_HEADERS` (por defecto: `*`)
- `CORS_EXPOSED_HEADERS` (por defecto: `Authorization`)
- `CORS_ALLOW_CREDENTIALS` (por defecto: `true`)
- `CORS_MAX_AGE` (por defecto: `3600`)

## Ejecutar proyecto

Activa el perfil `dev` para desarrollo local:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
```

```powershell
./mvnw.cmd spring-boot:run
```

Aplicacion por defecto en:

- `http://localhost:8080`

Swagger:

Disponible solo cuando `APP_DOCS_PUBLIC_ENABLED=true`.

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

## Correr tests

```powershell
./mvnw.cmd test
```

Los tests usan perfil `test` con base en memoria H2 (`src/test/resources/application-test.yml`).

## Endpoints disponibles

### Auth (publicos)

- `POST /api/auth/register`
- `POST /api/auth/login`

### User (protegido con JWT)

- `GET /api/users/{id}`

## Pruebas rapidas con cURL

### Register

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "Password123"
  }'
```

### Login

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'
```

Respuesta esperada (ejemplo):

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<JWT_TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "userId": 1,
    "email": "john@example.com",
    "role": "USER"
  }
}
```

Para endpoints protegidos:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Notas para extender el boilerplate

- Agregar refresh token endpoint (`/api/auth/refresh`).
- Mejorar manejo de excepciones por tipo (401, 403, 404, 422).
- Agregar migraciones (Flyway o Liquibase) para ambientes reales.
- Endurecer configuracion de `application-prod.yml` para produccion.

## Branch protection recomendada

Consulta la configuracion sugerida en `.github/branch-protection.md`.
