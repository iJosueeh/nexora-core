# Nexora Core Back-End Standards (Spring Boot & GraphQL)

Este documento define las reglas de ingeniería para el desarrollo del ecosistema Back-End de Nexora.

## 0. Sincronización Obligatoria
- **Antes de iniciar cualquier tarea:** Es mandatorio ejecutar `git pull origin dev` para asegurar que se está trabajando sobre la versión más reciente del código y evitar conflictos de fusión (merge conflicts).

## 1. Arquitectura y Organización
- **Feature-First Packaging:** Organizar el código por dominios de negocio (ej: `com.nexora.core.content`, `com.nexora.core.auth`).
- **Capas Internas:** Cada paquete de feature debe seguir esta estructura:
    - `.domain.entity`: Entidades JPA (Base de Datos).
    - `.domain.dto`: Objetos de transferencia para la API.
    - `.repository`: Interfaces de Spring Data JPA.
    - `.service`: Lógica de negocio e integraciones.
    - `.graphql`: Resolvers y controladores de la API.

## 2. API Contract (GraphQL)
- **Esquema Único:** El archivo `schema.graphqls` es el contrato de verdad. Cualquier cambio en la API debe iniciar aquí.
- **Tipado Estricto:** Usar `!` para campos obligatorios. No abusar de tipos genéricos.
- **Identificadores Públicos:** Prohibido exponer IDs autoincrementales de SQL. Usar `UUID` para registros y `slug` para recursos accesibles vía URL.
- **Optimización:** Implementar `@BatchMapping` o `DataLoader` para evitar el problema de consultas N+1.

## 3. Calidad de Código Java
- **Lombok:** Uso obligatorio para `Getters`, `Setters`, `Builders` y `Constructores` para mantener el código conciso.
- **Inmutabilidad:** Preferir el uso de `final` en campos inyectados y records para DTOs.
- **Validaciones:** Usar anotaciones de `jakarta.validation` (ej: `@NotBlank`, `@Email`) en los Inputs de GraphQL.

## 4. Persistencia y Base de Datos
- **Auditoría:** Todas las entidades de negocio deben heredar de `AuditableBaseEntity` para rastrear fechas de creación y actualización.
- **Naming:** Las tablas deben nombrarse en plural y usar `snake_case` (ej: `research_papers`).
- **Slugs:** Implementar lógica de generación automática de slugs en la capa de servicio para títulos de investigaciones y eventos.

## 5. Seguridad y Reglas de Negocio
- **Ownership:** Validar siempre que el usuario autenticado tenga permisos sobre el recurso que intenta modificar.
- **RSVP Uniqueness:** El sistema debe impedir que un usuario confirme asistencia al mismo evento más de una vez.

## 6. Validación y Calidad del Código
- **Build & Test Cycle:** Tras cada cambio en la lógica de negocio o persistencia, es obligatorio ejecutar `./mvnw clean compile` y los tests relacionados (`./mvnw test`).
- **Cobertura:** Todo nuevo Service debe contar con su respectivo test unitario (JUnit 5 + Mockito) que cubra casos de éxito y error (edge cases).
- **Eficiencia:** Verificar mediante logs o herramientas de profiling que no se generen consultas redundantes (N+1) en las relaciones JPA.

## 7. Validación de IA
- Antes de implementar un nuevo Resolver, verificar que la Entidad y el Repositorio soporten los campos solicitados.
