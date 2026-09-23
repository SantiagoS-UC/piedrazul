# Documentación

Aquí vive todo lo que alimenta el documento de arquitectura del primer corte. Los archivos de la
tabla se irán agregando a medida que avance el sprint.

| Carpeta / archivo | Contenido |
|---|---|
| `diagrams/` | Diagramas C4 y UML (4+1 vistas) como código PlantUML (`.puml`). |
| `prototypes/` | Prototipos de la interfaz y resultados del test de usabilidad. |
| `user-stories.md` | Historias de usuario con criterios de aceptación. |
| `quality-scenarios.md` | Escenarios de calidad de usabilidad y seguridad. |
| `design-patterns.md` | Patrones de diseño y arquitectura aplicados, con su contexto. |

El diagrama C4 de componentes no se dibuja a mano: lo genera `ModularityTest` en
`backend/target/spring-modulith-docs` a partir del código real.

## Checklist del documento de arquitectura

- [ ] Portada
- [ ] Introducción breve
- [ ] Historias de usuario con criterios de aceptación
- [ ] Prototipos de la interfaz y test de usabilidad
- [ ] Pantallazo del tablero del Sprint 1 (Jira o Trello)
- [ ] Escenarios de calidad de usabilidad y seguridad: contexto, estímulo, respuesta, medición y resultado esperado
- [ ] Arquitectura con el modelo C4
- [ ] Listado de patrones de diseño implementados y su contexto
