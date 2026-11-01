# Piedrazul

Sistema de reserva de citas médicas para la clínica Piedrazul. Proyecto del curso Ingeniería de
Software III, Universidad del Cauca, 2026.2.

- **Frontend:** SPA en Angular (TypeScript).
- **Backend:** monolito modular en Java 21 con Spring Boot 4 y Spring Modulith.
- **Base de datos:** PostgreSQL 17 en Docker, con migraciones de Flyway.
- **Autenticación:** Keycloak 26 en Docker. Emite los tokens JWT y guarda usuarios, contraseñas y roles.

## Estructura del repositorio

```
piedrazul/
├── backend/     API REST (monolito modular)
├── frontend/    SPA en Angular
├── docs/        diagramas C4 en PlantUML (el resto de la documentación está en Drive)
└── docker-compose.yml
```

### Módulos del backend

Paquete base `co.edu.unicauca.piedrazul`:

| Módulo | Responsabilidad |
|---|---|
| `identity` | Registro de pacientes, autenticación y roles |
| `professionals` | Médicos y terapistas |
| `configuration` | Parámetros de agendamiento de cada profesional |
| `appointments` | Franjas disponibles, agendamiento y consulta de citas |
| `shared` | Excepciones y tipos comunes |

Cada módulo se divide en `domain`, `application` e `infrastructure` (arquitectura hexagonal).
Un módulo solo puede usar los tipos del paquete raíz de otro; `ModularityTest` lo verifica.

## Requisitos

- JDK 21
- Node.js LTS y Angular CLI (`npm install -g @angular/cli`)
- Docker
- IntelliJ IDEA (el plugin PlantUML Integration sirve para ver los diagramas de `docs/`)

## Cómo ejecutarlo

1. Levantar la base de datos y Keycloak desde la raíz del repositorio:
   ```bash
   docker compose up -d
   ```
   Keycloak tarda cerca de un minuto en arrancar la primera vez. La consola de administración
   queda en `http://localhost:8180` (usuario `admin`, contraseña `admin`) y el realm de la
   aplicación es `piedrazul`, que se importa desde `keycloak/piedrazul-realm.json`.
2. Backend: ejecutar `PiedrazulApplication` desde IntelliJ, o desde `backend/`:
   ```bash
   ./mvnw spring-boot:run
   ```
   Queda en `http://localhost:8081`. `http://localhost:8081/actuator/health` debe responder `UP`.
   Si ese puerto también está ocupado en tu equipo, define la variable de entorno `SERVER_PORT`.
3. Frontend, desde `frontend/`:
   ```bash
   npm install
   npm start
   ```
   Queda en `http://localhost:4200`.

## Keycloak

| Cliente | Uso |
|---|---|
| `piedrazul-web` | Inicio de sesión desde el frontend (Authorization Code con PKCE). |
| `piedrazul-backend` | Cuenta de servicio con la que el backend crea los pacientes que se registran. |
| `piedrazul-dev-tools` | Solo para pruebas: permite pedir un token con usuario y contraseña desde Postman, IntelliJ u OWASP ZAP. |

La gestión de usuarios y roles se hace desde la consola de Keycloak o con la colección de
Postman de `docs/postman/`.

Si se cambia `piedrazul-realm.json`, hay que borrar el volumen para que se vuelva a importar:
`docker compose down` y luego `docker volume rm piedrazul_keycloak-data` (se pierden los
usuarios creados a mano).

## Pruebas

- Backend, desde `backend/`: `./mvnw test`. Incluye `ModularityTest`, que falla si un módulo usa
  clases internas de otro.
- Frontend, desde `frontend/`: `npx ng test --watch=false`.
- La API se puede probar a mano con los archivos de `backend/http/`, que se ejecutan desde IntelliJ.

## Usuarios de prueba

Están en Keycloak (`keycloak/piedrazul-realm.json`), con los mismos identificadores que usan las
migraciones de Flyway para los pacientes y los profesionales. Todas las contraseñas siguen el
formato `<usuario>-1234`.

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@gmail.com | admin-1234 |
| Agendador | agendador@gmail.com | agendador-1234 |
| Paciente | paciente1@gmail.com | paciente1-1234 |
| Paciente | paciente2@gmail.com | paciente2-1234 |
| Profesional (Medicina general) | laura.martinez@gmail.com | laura.martinez-1234 |
| Profesional (Medicina general) | andres.vargas@gmail.com | andres.vargas-1234 |
| Profesional (Terapia neural) | diana.rojas@gmail.com | diana.rojas-1234 |
| Profesional (Terapia neural) | jorge.castillo@gmail.com | jorge.castillo-1234 |
| Profesional (Quiropraxia) | natalia.guzman@gmail.com | natalia.guzman-1234 |
| Profesional (Quiropraxia) | camilo.benavides@gmail.com | camilo.benavides-1234 |
| Profesional (Fisioterapia) | paula.zambrano@gmail.com | paula.zambrano-1234 |
| Profesional (Fisioterapia) | mauricio.lopez@gmail.com | mauricio.lopez-1234 |

También se cargan ocho profesionales, dos por especialidad, y la disponibilidad de seis de ellos.
Jorge Castillo y Mauricio López quedan sin configurar a propósito, para mostrar que un profesional
sin disponibilidad no aparece al agendar.

## Convenciones

- **Nombrado:** carpetas, archivos, clases, métodos, variables, endpoints y tablas en inglés.
  Textos que ve el usuario, comentarios y documentación en español.
- **Comentarios:** solo donde aportan (el porqué, no el qué).
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/es/) con descripción en español:
  `feat: agrega registro de pacientes`, `fix: corrige cálculo de franjas`, `docs:`, `test:`, `refactor:`, `chore:`.

## Flujo de trabajo con Git

Ramas:

- `main`: solo versiones entregables. Se actualiza desde `develop` al cerrar un corte.
- `develop`: integración del sprint.
- `feature/<descripcion-corta>`: una por tarea, creada desde `develop`.

Ciclo de cada tarea:

```bash
git checkout develop
git pull
git checkout -b feature/patient-registration
# ...trabajar...
git add .
git commit -m "feat: agrega registro de pacientes"
git push -u origin feature/patient-registration
```

Luego se abre un Pull Request de la feature hacia `develop` en GitHub. Otro integrante lo revisa
y lo aprueba antes de hacer merge. Después del merge se puede borrar la rama
