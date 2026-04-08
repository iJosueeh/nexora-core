# Nexora Core - Backend Feature Docs

Este directorio contiene la documentacion funcional y tecnica por modulo (feature) del backend.

## Objetivo

- Estandarizar como documentamos logica de negocio.
- Dejar claras reglas, validaciones y contratos API.
- Facilitar onboarding del equipo backend.
- Usar esta estructura como base para nuevos modulos.

## Estructura

```text
docs/
  README.md
  features/
    _template/
      README.md
    auth/
      README.md
    user/
      README.md
    security/
      README.md
    graphql/
      README.md
```

## Como documentar un nuevo modulo

1. Crear carpeta en `docs/features/<nombre-modulo>/`.
2. Copiar la plantilla desde `docs/features/_template/README.md`.
3. Completar cada seccion con comportamiento real del codigo.
4. Agregar referencias a clases, endpoints y propiedades.
5. Mantener el documento actualizado con cada cambio funcional.

## Convenciones recomendadas

- Documentar primero reglas de negocio, luego detalles tecnicos.
- Incluir validaciones de entrada y errores esperados.
- Mencionar efectos colaterales (persistencia, seguridad, eventos).
- Declarar TODOs y decisiones pendientes al final.
