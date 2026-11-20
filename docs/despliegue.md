# Despliegue y CI/CD

## Despliegue en la nube

Todos los servicios usan planes gratuitos que no piden tarjeta de crédito.

| Pieza | Servicio | Dirección |
|---|---|---|
| Aplicación web | Render, sitio estático | https://piedrazul-web.onrender.com |
| API | Render, servicio web con Docker (Virginia) | https://piedrazul-api.onrender.com |
| Base de datos | Neon, PostgreSQL (AWS us-east-1) | Solo accesible por el backend |
| Keycloak | Cloud-IAM, plan Freemium | https://lemur-0.cloud-iam.com/auth (realm `piedrazul`) |

El diagrama está en `diagrams/uml/9-despliegue-nube.puml`.

- **Render** construye el backend con `backend/Dockerfile` y el frontend con `ng build`. Los dos
  servicios están descritos en `render.yaml` (Blueprint).
- **Neon** guarda la base de datos de producción. Flyway aplica las migraciones al arrancar el
  backend, igual que en local.
- **Cloud-IAM** aloja Keycloak. El realm se configura con `keycloak/piedrazul-realm-cloud.json`,
  que tiene los roles, los clientes con las direcciones de producción y los usuarios de prueba con
  los mismos ids de las migraciones, pero sin contraseñas: esas se definen en la consola de
  Keycloak y no quedan en el repositorio.
- Los datos sensibles (contraseña de la base de datos, secreto del cliente `piedrazul-backend`)
  son variables de entorno de Render.

**Limitación del plan gratuito:** el backend se apaga tras 15 minutos sin peticiones y tarda
cerca de un minuto en volver a responder.

### Variables de entorno del backend en Render

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host de Neon>/neondb?sslmode=require` |
| `DB_USERNAME`, `DB_PASSWORD` | Usuario y contraseña de Neon |
| `KEYCLOAK_URL` | `https://lemur-0.cloud-iam.com/auth` |
| `KEYCLOAK_ISSUER_URI` | `https://lemur-0.cloud-iam.com/auth/realms/piedrazul` |
| `KEYCLOAK_CLIENT_SECRET` | Secreto del cliente `piedrazul-backend` en Cloud-IAM |
| `CORS_ALLOWED_ORIGINS` | `https://piedrazul-web.onrender.com` |

## Esquema de CI/CD

El diagrama está en `diagrams/uml/10-ci-cd.puml`.

| Etapa | Cuándo | Qué hace |
|---|---|---|
| Integración continua (`.github/workflows/ci.yml`) | Cada PR y cada push a `develop` | Compila y corre las pruebas del backend (`./mvnw verify`) y del frontend (`ng test`, `ng build`) en paralelo |
| Despliegue continuo (`.github/workflows/cd.yml`) | Cada push a `main` | Vuelve a correr la integración continua; si pasa, llama a los deploy hooks de Render y verifica que `/actuator/health` responda `UP` |

En Render el despliegue automático está desactivado (`autoDeployTrigger: off`): solo despliega el
workflow de CD, así nunca se publica una versión que no haya pasado las pruebas.

Los deploy hooks son secrets del repositorio en GitHub: `RENDER_DEPLOY_HOOK_API` y
`RENDER_DEPLOY_HOOK_WEB`.
