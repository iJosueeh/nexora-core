# Nexora - Red Social Universitaria (Backend Core)

Nexora es una red social universitaria para estudiantes de la UTP, inspirada en la experiencia de Twitter. Este repositorio contiene el backend core de la aplicacion.

## Descripcion

NexoraApp permite:

- Publicar mensajes
- Comentar publicaciones
- Dar like
- Compartir publicaciones
- Gestionar perfiles y contenido multimedia

El backend esta construido con Spring Boot y expone APIs REST (autenticacion y usuarios) con integracion inicial de GraphQL.

## Tecnologias

- Java 21
- Spring Boot 4.0.3
- Spring Security + JWT
- Spring Data JPA
- GraphQL (Spring GraphQL)
- PostgreSQL (runtime)
- H2 (tests)
- OpenAPI/Swagger (opcional en desarrollo)

## Estructura del backend

```text
src/main/java/com/nexora/core
  auth/
  common/
  graphql/
  security/
  user/
```

Clase principal:

- `NexoraAppApplication`

## Documentacion por feature

La guia para el equipo backend esta en:

- `docs/README.md`

Cada modulo tiene su propio documento en:

- `docs/features/auth/README.md`
- `docs/features/user/README.md`
- `docs/features/security/README.md`
- `docs/features/graphql/README.md`

Plantilla base para nuevos modulos:

- `docs/features/_template/README.md`

## Variables de entorno requeridas

Configura estas variables para ejecutar en local:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`

Variables opcionales:

- `SPRING_PROFILES_ACTIVE` (recomendado: `dev`)
- `APP_DOCS_PUBLIC_ENABLED` (por defecto: `false`)
- `APP_GRAPHQL_PUBLIC_ENABLED` (por defecto: `true`)
- `GRAPHQL_GRAPHIQL_ENABLED` (por defecto: `true`)

Ejemplo de `.env`:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/nexora
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change-this-secret-key-at-least-32-characters
JWT_REFRESH_SECRET=change-this-refresh-secret-key-at-least-32-chars
SPRING_PROFILES_ACTIVE=dev
APP_DOCS_PUBLIC_ENABLED=true
APP_GRAPHQL_PUBLIC_ENABLED=true
GRAPHQL_GRAPHIQL_ENABLED=true
```

## Instalacion y ejecucion

```powershell
./mvnw.cmd clean spring-boot:run
```

La API inicia por defecto en:

- `http://localhost:8080`

## GraphQL integrado

Endpoint GraphQL:

- `POST /graphql`

IDE GraphiQL:

- `GET /graphiql`

Schema inicial:

```graphql
type Query {
  health: String!
}
```

Consulta de ejemplo:

```graphql
query {
  health
}
```

Respuesta esperada:

```json
{
  "data": {
    "health": "Nexora GraphQL API is running"
  }
}
```

## Endpoints REST actuales

Publicos:

- `POST /api/auth/register`
- `POST /api/auth/login`

Protegidos con JWT:

- `GET /api/users/{id}`

## Testing

```powershell
./mvnw.cmd test
```

Los tests usan perfil `test` con H2 en memoria.

## Deploy en Render

Este repositorio incluye [render.yaml](render.yaml) para desplegar el backend en Render.

### Configuracion recomendada

- Runtime: `Java`
- Build command: `./mvnw -DskipTests clean package`
- Start command: `java -jar target/nexora-core-0.0.1-SNAPSHOT.jar`
- Health check path: `/actuator/health`

El backend toma el puerto automaticamente desde `PORT`:

- [src/main/resources/application.yml](src/main/resources/application.yml)
  - `server.port: ${PORT:8080}`

### Variables de entorno obligatorias en Render

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`
- `SUPABASE_URL`
- `SUPABASE_KEY`
- `CORS_ALLOWED_ORIGINS`

### Variables recomendadas

- `SPRING_PROFILES_ACTIVE=prod`
- `APP_DOCS_PUBLIC_ENABLED=false`
- `APP_GRAPHQL_PUBLIC_ENABLED=true`
- `GRAPHQL_GRAPHIQL_ENABLED=false`

## Frontend (referencia del proyecto general)

Stack del frontend de Nexora:

- Angular

Comandos esperados en el repo frontend:

```bash
npm install
ng serve
ng test
```

## Equipo de desarrollo

- @Crismar12
- @kath144 (Katherine Salas)
- @KennySth

## Licencia

Proyecto academico para la UTP. Uso educativo.
