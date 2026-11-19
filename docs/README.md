# Documentación

El documento de arquitectura, las historias de usuario, los prototipos, el test de usabilidad y los
escenarios de calidad se trabajan en la carpeta compartida del equipo en Google Drive.

Aquí quedan los diagramas que conviene versionar junto al código, en formato PlantUML (`.puml`).

| Carpeta | Archivo | Diagrama |
|---|---|---|
| `diagrams/c4` | `1-contexto.puml` | C4 nivel 1: contexto |
| `diagrams/c4` | `2-contenedores.puml` | C4 nivel 2: contenedores |
| `diagrams/c4` | `3a-componentes-backend.puml` | C4 nivel 3: componentes de la API |
| `diagrams/c4` | `3b-componentes-frontend.puml` | C4 nivel 3: componentes de la aplicación web |
| `diagrams/c4` | `4a-codigo-identity.puml` | C4 nivel 4: registro con Keycloak |
| `diagrams/c4` | `4b-codigo-appointments.puml` | C4 nivel 4: exportación e historial de citas |
| `diagrams/c4` | `4c-codigo-frontend.puml` | C4 nivel 4: páginas del personal |
| `diagrams/uml` | `5-uml-escenarios.puml` | 4+1, vista de escenarios: casos de uso |
| `diagrams/uml` | `6-uml-proceso.puml` | 4+1, vista de procesos: exportar citas |
| `diagrams/uml` | `7-uml-desarrollo.puml` | 4+1, vista de desarrollo: paquetes |
| `diagrams/uml` | `8-uml-fisica.puml` | 4+1, vista física: despliegue |

La vista lógica del modelo 4+1 corresponde a los diagramas de clases del nivel 4 del C4.

Los diagramas se abren con el plugin PlantUML Integration de IntelliJ y no necesitan Graphviz
(usan `!pragma layout smetana`).

## Postman

La colección `postman/piedrazul-keycloak.postman_collection.json` crea usuarios, les asigna roles y
prueba el acceso a la API (requisito no funcional 1). Se importa en Postman con *Import* y se
ejecuta en orden, empezando por "Token del administrador".
