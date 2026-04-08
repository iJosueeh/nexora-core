# Feature: Auth

## 1. Objetivo del modulo

Gestionar registro e inicio de sesion de usuarios para emitir JWT de acceso en Nexora.

## 2. Alcance

- Incluye registro de usuario y login por email/password.
- Incluye generacion de access token JWT.
- No incluye refresh token (pendiente).

## 3. Logica de negocio

- Registro:
  - Si el email ya existe, se rechaza la operacion.
  - El password se almacena hasheado con BCrypt.
  - Usuario nuevo inicia activo (`isActive=true`) y con rol `USER`.
- Login:
  - Se autentica con `AuthenticationManager`.
  - Si credenciales son validas, se retorna token JWT.
- Respuesta de auth:
  - Incluye token, tipo (`Bearer`), expiracion, userId, email y role.

## 4. Validaciones

### Entrada

- `RegisterRequest.username`: requerido, min 3, max 60.
- `RegisterRequest.email`: requerido, formato email valido.
- `RegisterRequest.password`: requerido, min 8, max 72.
- `LoginRequest.email`: requerido, formato email valido.
- `LoginRequest.password`: requerido, min 8, max 72.

### Reglas de dominio

- Email unico (`existsByEmail`).
- Credenciales invalidas generan error de autenticacion.

## 5. Contratos API

### REST

- `POST /api/auth/register`
  - 201 cuando registra correctamente.
  - 400 si email ya existe.
  - 422 en errores de validacion.
- `POST /api/auth/login`
  - 200 cuando autentica correctamente.
  - 401 en credenciales invalidas.
  - 422 en errores de validacion.

## 6. Persistencia

- Entidad: `User`.
- Repositorio: `UserRepository`.

## 7. Seguridad

- Endpoints publicos: `/api/auth/**`.
- No requiere JWT para login/registro.

## 8. Errores y excepciones

- `IllegalArgumentException("Email is already registered")` -> 400.
- `BadCredentialsException` -> 401.
- `MethodArgumentNotValidException` -> 422.

## 9. Dependencias del modulo

- `PasswordEncoder`
- `AuthenticationManager`
- `JwtService`
- `UserRepository`

## 10. Observabilidad

- No se registran logs de negocio explicitos en `AuthService` actualmente.
- Recomendado: loguear intentos de login fallido sin exponer datos sensibles.

## 11. Casos de prueba sugeridos

- Registro exitoso retorna JWT.
- Login con password incorrecto falla.
- Registro duplicado por email falla.
- Validaciones de DTO en requests invalidos.

## 12. TODO / Pendientes

- Agregar refresh token.
- Agregar logout con invalidacion de token (si se implementa blacklist/rotacion).
- Evaluar verificacion de email para activacion de cuenta.
