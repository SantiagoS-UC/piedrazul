package co.edu.unicauca.piedrazul;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Monitoreo: los endpoints de salud que usa Render y el identificador de petición de los logs.
 */
@SpringBootTest
@Import(IntegrationTestConfiguration.class)
class MonitoringIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // MockMvc no agrega solo los filtros de la aplicación: el de logs va primero, como en el servidor.
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("requestLoggingFilter", Filter.class))
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("La salud del backend es pública e incluye el estado de la base de datos")
    void healthIncludesDatabase() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    @DisplayName("Las sondas de vida y de disponibilidad responden sin iniciar sesión")
    void probesArePublic() throws Exception {
        mvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("La información de la aplicación es pública")
    void infoIsPublic() throws Exception {
        mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Piedrazul"));
    }

    @Test
    @DisplayName("Cada respuesta devuelve el id de la petición, aunque sea rechazada por seguridad")
    void everyResponseHasRequestId() throws Exception {
        mvc.perform(get("/api/appointments/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-Id", matchesPattern("[0-9a-f-]{36}")));
        mvc.perform(get("/api/appointments/mine").header("X-Request-Id", "demo-123"))
                .andExpect(header().string("X-Request-Id", "demo-123"));
    }

    @Test
    @DisplayName("Los demás endpoints de Actuator no están publicados")
    void otherActuatorEndpointsAreHidden() throws Exception {
        mvc.perform(get("/actuator/env"))
                .andExpect(status().is4xxClientError());
    }
}
