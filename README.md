# Piedrazul

Sistema de reserva de citas médicas para la clínica Piedrazul. Proyecto del curso Ingeniería de
Software III, Universidad del Cauca, 2026.2.

- **Frontend:** SPA en Angular (TypeScript).
- **Backend:** monolito modular en Java 21 con Spring Boot 4 y Spring Modulith.
- **Base de datos:** PostgreSQL 17 en Docker, con migraciones de Flyway.

## Estructura del repositorio

```
piedrazul/
├── backend/     API REST (monolito modular)
├── frontend/    SPA en Angular
├── docs/        documento de arquitectura, diagramas y prototipos
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
- IntelliJ IDEA con el plugin plantuml4idea

## Cómo ejecutarlo

1. Levantar la base de datos desde la raíz del repositorio:
   ```bash
   docker compose up -d
   ```
2. Backend: ejecutar `PiedrazulApplication` desde IntelliJ, o desde `backend/`:
   ```bash
   ./mvnw spring-boot:run
   ```
   Queda en `http://localhost:8080`. `http://localhost:8080/actuator/health` debe responder `UP`.
3. Frontend, desde `frontend/`:
   ```bash
   npm install
   npm start
   ```
   Queda en `http://localhost:4200`.

Pruebas del backend: `./mvnw test`.

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
y lo aprueba antes de hacer merge. Después del merge se puede borrar la rama.
