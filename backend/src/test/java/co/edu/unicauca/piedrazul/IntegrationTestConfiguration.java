package co.edu.unicauca.piedrazul;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Entorno de las pruebas de integración y de aceptación: un PostgreSQL 17 en Docker (la misma
 * versión de docker-compose.yml y de Neon) con las migraciones de Flyway y los datos semilla, y un
 * reloj fijo para que las fechas de las pruebas no dependan del día en que se ejecutan.
 *
 * <p>Spring reutiliza el contexto entre clases de prueba con esta misma configuración, así que el
 * contenedor se levanta una sola vez por ejecución.
 */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfiguration {

    /** Lunes 28 de septiembre de 2026 a las 9:10 a. m., hora de Colombia. */
    public static final Instant NOW = Instant.parse("2026-09-28T14:10:00Z");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

    // Tiene prioridad sobre el reloj del sistema de ClockConfiguration.
    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(NOW, ZoneId.of("America/Bogota"));
    }
}
