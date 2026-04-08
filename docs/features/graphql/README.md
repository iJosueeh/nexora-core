# Feature: GraphQL

## 1. Objetivo del modulo

Introducir capa GraphQL en Nexora para consultas flexibles desde frontend.

## 2. Alcance

- Incluye schema base y query de salud.
- Incluye habilitacion de GraphiQL.
- No incluye aun queries/mutations de dominio (posts, comentarios, likes).

## 3. Logica de negocio

- Query `health` retorna estado simple de disponibilidad API.

## 4. Validaciones

### Entrada

- No recibe argumentos en la query actual `health`.

### Reglas de dominio

- Ninguna regla de negocio compleja en estado actual.

## 5. Contratos API

### GraphQL

- Endpoint: `POST /graphql`
- Query disponible:

```graphql
query {
  health
}
```

- Respuesta esperada:

```json
{
  "data": {
    "health": "Nexora GraphQL API is running"
  }
}
```

- IDE local: `GET /graphiql`

## 6. Persistencia

- No accede a base de datos actualmente.

## 7. Seguridad

- Exposicion publica controlada por `app.graphql.public-enabled`.

## 8. Errores y excepciones

- Errores de schema/resolver se exponen bajo formato GraphQL estandar.

## 9. Dependencias del modulo

- `spring-boot-starter-graphql`
- `NexoraQueryController`
- `schema.graphqls`

## 10. Observabilidad

- Recomendado: agregar metricas por operacion GraphQL y tiempos de resolucion.

## 11. Casos de prueba sugeridos

- Query `health` retorna 200 y payload esperado.
- GraphiQL habilitado/deshabilitado por propiedad.

## 12. TODO / Pendientes

- Diseñar schema para publicaciones, comentarios, likes, shares y perfiles.
- Agregar queries paginadas y mutations de creacion/edicion.
- Evaluar DataLoader para evitar N+1 cuando crezca el dominio.
