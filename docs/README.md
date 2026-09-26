# Documentación

El documento de arquitectura, las historias de usuario, los prototipos, el test de usabilidad y los
escenarios de calidad se trabajan en la carpeta compartida del equipo en Google Drive.

Aquí solo quedan los diagramas que conviene versionar junto al código:

| Archivo | Contenido |
|---|---|
| `diagrams/c4-context.puml` | Diagrama C4 de contexto |
| `diagrams/c4-container.puml` | Diagrama C4 de contenedores |

Se abren con el plugin PlantUML Integration de IntelliJ. El diagrama C4 de componentes no se dibuja a
mano: lo genera `ModularityTest` en `backend/target/spring-modulith-docs` a partir del código real,
cada vez que se corren las pruebas del backend.
