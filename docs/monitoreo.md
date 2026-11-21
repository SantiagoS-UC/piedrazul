# Monitoreo

Requisito no funcional 6 (opcional): estado de salud del backend, login centralizado y monitoreo de
logs. Todo funciona con los planes gratuitos del despliegue (ver [despliegue.md](despliegue.md)).

## Salud del backend

Spring Boot Actuator publica estos endpoints, sin iniciar sesión:

| Endpoint | Qué responde | Quién lo usa |
|---|---|---|
| `/actuator/health` | Estado general y de cada componente: base de datos (`db`), disco y sondas | Health check de Render antes de dar por bueno un despliegue, y el paso de *smoke test* del workflow de CD |
| `/actuator/health/liveness` | Si el proceso está vivo. No consulta la base de datos | Monitores externos |
| `/actuator/health/readiness` | Si puede recibir tráfico | Monitores externos |
| `/actuator/info` | Nombre de la aplicación y versión de Java | Diagnóstico |

Solo se publican `health` e `info`; el resto de endpoints de Actuator (variables de entorno,
métricas internas) no está expuesto. La respuesta de salud muestra el estado de cada componente
pero no detalles internos como la URL de la base de datos.

En producción: https://piedrazul-api.onrender.com/actuator/health

## Logs

`RequestLoggingFilter` escribe una línea por cada petición a la API:

```
INFO ... [6f1c2e7a-...] c.e.u.p.s.i.web.RequestLoggingFilter : POST /api/appointments -> 201 (48 ms)
WARN ... [b9d0...]      c.e.u.p.s.i.web.RequestLoggingFilter : GET /api/appointments/report/monthly -> 200 (2310 ms) lenta
ERROR ... [0a4f...]     c.e.u.p.s.i.web.RequestLoggingFilter : PATCH /api/appointments/... -> 500 (12 ms)
```

- Cada petición tiene un identificador que aparece entre corchetes en **todas** sus líneas de log
  (también en los errores que registra `GlobalExceptionHandler`) y se devuelve en la cabecera
  `X-Request-Id`. Si un usuario reporta un error, con ese id se encuentra todo lo que pasó.
- El nivel indica la gravedad: `INFO` normal, `WARN` si tardó 2 segundos o más, `ERROR` si el
  servidor falló (5xx).
- No se registran datos personales: ni el usuario, ni el cuerpo de la petición, ni los parámetros
  de la URL, que pueden llevar números de documento.

Dónde verlos:

- **Local:** en la consola de IntelliJ o de `./mvnw spring-boot:run`.
- **Producción:** en Render, servicio `piedrazul-api` → pestaña **Logs**. Tiene buscador: se puede
  filtrar por el id de la petición, por `ERROR` o por una ruta.

## Login centralizado

Keycloak (Cloud-IAM en producción) es el único lugar donde se inicia sesión, para todos los roles,
y la protección contra fuerza bruta bloquea temporalmente una cuenta tras 5 intentos fallidos. Si
se activa **Realm settings → Events → User events settings → Save events**, la pestaña **Events**
de la consola lista los inicios de sesión y los intentos fallidos.

## Otros tableros

- **Neon** → pestaña **Monitoring**: uso de CPU, memoria y conexiones de la base de datos.
- **GitHub Actions** → pestaña **Actions**: resultado de cada ejecución de CI y CD.

**Monitor externo (opcional):** un servicio gratuito como UptimeRobot puede consultar
`/actuator/health/liveness` cada pocos minutos y avisar por correo si la API no responde. Ojo: esas
consultas mantienen despierto el backend de Render, y el health check de Render consulta la base de
datos, así que Neon tampoco se suspende y consume horas del plan gratuito. Conviene activarlo solo
en días clave, como el de la sustentación.
