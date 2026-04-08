# Feature: User

## 1. Objetivo del modulo

Exponer datos basicos del usuario para consumo de clientes autenticados.

## 2. Alcance

- Incluye consulta de usuario por ID.
- Incluye mapeo de entidad a DTO `UserResponse`.
- No incluye edicion de perfil ni listado/paginacion.

## 3. Logica de negocio

- Busca usuario por `id`.
- Si existe, retorna DTO con id, username, email, estado y rol.
- Si no existe, lanza `ResourceNotFoundException`.

## 4. Validaciones

### Entrada

- `id` llega por path variable (tipo `UUID`).

### Reglas de dominio

- El usuario consultado debe existir en base de datos.

## 5. Contratos API

### REST

- `GET /api/users/{id}`
  - 200 con `UserResponse` cuando existe.
  - 404 cuando no existe.

## 6. Persistencia

- Entidad: `User`.
- PK: `UUID` generado en `BaseEntity`.
- Repositorio: `UserRepository#findById(UUID)`.

## 7. Seguridad

- Endpoint protegido por JWT (no esta en lista publica).

## 8. Errores y excepciones

- `ResourceNotFoundException("User not found")` -> 404.

## 9. Dependencias del modulo

- `UserRepository`
- `ResourceNotFoundException`

## 10. Observabilidad

- Logs informativos en `UserService`:
  - Busqueda por id.
  - Usuario encontrado/no encontrado.

## 11. Casos de prueba sugeridos

- Usuario existente retorna 200 y payload correcto.
- Usuario inexistente retorna 404.
- Request sin token retorna 401/403 segun configuracion de seguridad.

## 12. TODO / Pendientes

- Agregar endpoint de perfil propio (`/api/users/me`).
- Agregar actualizacion de perfil y avatar.
- Considerar ocultar email para ciertos roles/escenarios.
