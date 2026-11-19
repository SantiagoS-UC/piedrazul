package co.edu.unicauca.piedrazul.shared.infrastructure;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClockConfiguration {

    // Todas las reglas de fechas (hoy, ventana de agendamiento, franjas pasadas) se evalúan en la
    // hora de la clínica, sin importar la zona horaria del servidor. En las pruebas se usa un reloj fijo.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
