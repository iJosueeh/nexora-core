# Feature: Security

## 1. Objetivo del modulo

Centralizar autenticacion JWT y politicas de acceso para endpoints REST y GraphQL.

## 2. Alcance

- Incluye filtro JWT por request.
- Incluye definicion de rutas publicas/protegidas.
- Incluye configuracion CORS.
- No incluye autorizacion por permisos finos (solo autenticacion global).

## 3. Logica de negocio

- Sesion stateless (`SessionCreationPolicy.STATELESS`).
- `JwtAuthenticationFilter` se ejecuta antes de `UsernamePasswordAuthenticationFilter`.
- Si hay token Bearer valido, se inyecta autenticacion en el contexto.
- Si no hay token o no aplica, la request continua y luego se valida acceso por reglas.

## 4. Validaciones

### Entrada

- Header `Authorization` con prefijo `Bearer ` para endpoints protegidos.

### Reglas de dominio

- Rutas publicas:
  - `/api/auth/**`
  - `/swagger-ui/**` y `/v3/api-docs/**` si `app.docs.public-enabled=true`
  - `/graphql`, `/graphiql`, `/graphiql/**` si `app.graphql.public-enabled=true`
- Cualquier otra ruta requiere autenticacion.

## 5. Contratos API

### REST

- 401/403 cuando falta token o es invalido en recursos protegidos.

### GraphQL

- Acceso publico configurable por propiedad.

## 6. Persistencia

- No persiste datos directamente.

## 7. Seguridad

- Password hashing: BCrypt (`PasswordEncoder`).
- JWT contiene al menos subject(email) y claim role.
- CSRF deshabilitado por arquitectura stateless con JWT.

## 8. Errores y excepciones

- JWT invalido/expirado -> `IllegalArgumentException` desde `JwtService`.
- Credenciales invalidas -> `BadCredentialsException`.

## 9. Dependencias del modulo

- `JwtService`
- `JwtAuthenticationFilter`
- `CustomUserDetailsService`
- Spring Security

## 10. Observabilidad

- `JwtAuthenticationFilter` loguea autenticaciones exitosas en nivel debug.

## 11. Casos de prueba sugeridos

- Endpoint protegido sin token -> denegado.
- Endpoint protegido con token valido -> permitido.
- GraphQL publico on/off segun propiedad.
- CORS con origen permitido y no permitido.

## 12. TODO / Pendientes

- Definir estrategia de revocacion de tokens.
- Agregar control por roles/permisos para endpoints de administracion.
