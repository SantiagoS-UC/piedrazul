package co.edu.unicauca.piedrazul.acceptance;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unicauca.piedrazul.IntegrationTestConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de aceptación: recorren la API completa (seguridad, controladores, servicios y
 * PostgreSQL real) siguiendo los criterios de aceptación de las historias de usuario, escritos
 * como Dado / Cuando / Entonces.
 *
 * <p>Los tokens de Keycloak se simulan con el mismo id (claim sub) y rol que tendría cada usuario
 * de prueba. "Hoy" es el lunes 28 de septiembre de 2026 a las 9:10 a. m.
 */
@SpringBootTest
@Import(IntegrationTestConfiguration.class)
class AppointmentsAcceptanceTest {

    // Datos semilla de las migraciones.
    private static final String ANA = "00000000-0000-0000-0000-000000000101";
    private static final String CARLOS = "00000000-0000-0000-0000-000000000102";
    private static final String LAURA = "20000000-0000-0000-0000-000000000001";
    private static final String ANDRES = "20000000-0000-0000-0000-000000000002";

    // Laura Martínez atiende de lunes a viernes, de 8:00 a 12:00, cada 20 minutos.
    private static final String TOMORROW = "2026-09-29";
    private static final String WEDNESDAY = "2026-09-30";
    private static final String THURSDAY = "2026-10-01";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM appointments");
        // UTF-8 explícito para comparar los mensajes con tildes tal como los recibe el navegador.
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }

    @Nested
    @DisplayName("HU: el paciente agenda su cita")
    class PatientBooking {

        @Test
        @DisplayName("Dado un paciente registrado, cuando agenda una franja libre, entonces la cita aparece en "
                + "sus citas y en la agenda del profesional")
        void bookedAppointmentIsVisibleToEveryone() throws Exception {
            book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(get("/api/appointments/mine").with(patient(ANA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].date").value(WEDNESDAY))
                    .andExpect(jsonPath("$[0].status").value("UPCOMING"))
                    .andExpect(jsonPath("$[0].cancellable").value(true));
            mvc.perform(get("/api/appointments/agenda").param("date", WEDNESDAY).with(professional(LAURA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.appointments[0].patient.documentNumber").value("1061000001"));
        }

        @Test
        @DisplayName("Dado que otro paciente ya tomó una franja, cuando intento agendarla, entonces el sistema "
                + "me pide elegir otra hora")
        void takenSlotIsRejected() throws Exception {
            book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(bookRequest(LAURA, WEDNESDAY, "10:00").with(patient(CARLOS)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("Elige otra hora")));
        }
    }

    @Nested
    @DisplayName("HU: el agendador crea citas")
    class SchedulerBooking {

        @Test
        @DisplayName("Dado un paciente que llama a la clínica, cuando el agendador le asigna una franja, "
                + "entonces la cita queda a nombre del paciente")
        void schedulerBooksForPatient() throws Exception {
            mvc.perform(post("/api/appointments/for-patient").with(scheduler())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json("""
                                    {"patientId": "%s", "professionalId": "%s", "date": "%s", "time": "08:20"}
                                    """, CARLOS, LAURA, WEDNESDAY)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientName").value("Carlos Andrés Muñoz Pérez"));

            mvc.perform(get("/api/appointments/mine").with(patient(CARLOS)))
                    .andExpect(jsonPath("$[0].time").value("08:20"));
        }

        @Test
        @DisplayName("Un paciente no puede agendar a nombre de otra persona")
        void patientCannotBookForOthers() throws Exception {
            mvc.perform(post("/api/appointments/for-patient").with(patient(ANA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json("""
                                    {"patientId": "%s", "professionalId": "%s", "date": "%s", "time": "08:20"}
                                    """, CARLOS, LAURA, WEDNESDAY)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("HU: el profesional reagenda una cita")
    class Rescheduling {

        @Test
        @DisplayName("Dada una cita en su agenda, cuando el profesional la mueve a otra franja libre, entonces "
                + "la cita cambia de fecha y la franja anterior queda libre")
        void professionalReschedules() throws Exception {
            String id = book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(patch("/api/appointments/{id}", id).with(professional(LAURA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json("""
                                    {"date": "%s", "time": "08:00"}
                                    """, THURSDAY)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value(THURSDAY))
                    .andExpect(jsonPath("$.time").value("08:00"));

            mvc.perform(get("/api/appointments/agenda").param("date", WEDNESDAY).with(professional(LAURA)))
                    .andExpect(jsonPath("$.total").value(0));
            book(CARLOS, LAURA, WEDNESDAY, "10:00");
        }

        @Test
        @DisplayName("Un profesional no puede reagendar citas de la agenda de otro")
        void otherProfessionalIsForbidden() throws Exception {
            String id = book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(patch("/api/appointments/{id}", id).with(professional(ANDRES))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json("""
                                    {"date": "%s", "time": "08:00"}
                                    """, THURSDAY)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("HU: el paciente cancela una cita")
    class Cancellation {

        @Test
        @DisplayName("Dada una cita dentro de dos días o más, cuando el paciente la cancela con un motivo, "
                + "entonces queda cancelada y otro paciente puede tomar esa franja")
        void cancellationFreesSlot() throws Exception {
            String id = book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(cancelRequest(id, "Tengo un viaje de trabajo").with(patient(ANA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.cancellationReason").value("Tengo un viaje de trabajo"));

            book(CARLOS, LAURA, WEDNESDAY, "10:00");
            mvc.perform(get("/api/appointments/patient-history").param("patientId", ANA).with(scheduler()))
                    .andExpect(jsonPath("$.cancelled").value(1))
                    .andExpect(jsonPath("$.appointments[0].cancellationReason").value("Tengo un viaje de trabajo"));
        }

        @Test
        @DisplayName("Dada una cita para mañana, cuando el paciente intenta cancelarla, entonces el sistema le "
                + "explica que se cancela con mínimo dos días de anticipación")
        void lessThanTwoDaysIsRejected() throws Exception {
            String id = book(ANA, LAURA, TOMORROW, "10:00");

            mvc.perform(cancelRequest(id, "Tengo un viaje de trabajo").with(patient(ANA)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("mínimo 2 días de anticipación")));
        }

        @Test
        @DisplayName("Cuando el paciente no escribe el motivo, entonces el sistema se lo pide")
        void reasonIsRequired() throws Exception {
            String id = book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(cancelRequest(id, "").with(patient(ANA)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.reason").value("Escribe el motivo de la cancelación."));
        }

        @Test
        @DisplayName("Un paciente no puede cancelar la cita de otro")
        void otherPatientIsForbidden() throws Exception {
            String id = book(ANA, LAURA, WEDNESDAY, "10:00");

            mvc.perform(cancelRequest(id, "Tengo un viaje de trabajo").with(patient(CARLOS)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("HU: el profesional consulta el reporte de citas por mes")
    class MonthlyReport {

        @Test
        @DisplayName("Dadas citas en varios meses, cuando el profesional pide el reporte del año, entonces ve "
                + "cuántas citas vigentes tuvo cada profesional por mes")
        void countsScheduledAppointments() throws Exception {
            book(ANA, LAURA, WEDNESDAY, "10:00");
            book(CARLOS, LAURA, "2026-10-14", "08:00");
            String cancelled = book(ANA, ANDRES, "2026-10-07", "14:00");
            mvc.perform(cancelRequest(cancelled, "Ya no necesito la consulta").with(patient(ANA)))
                    .andExpect(status().isOk());

            mvc.perform(get("/api/appointments/report/monthly").param("year", "2026").with(professional(LAURA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(2))
                    .andExpect(jsonPath("$.monthTotals[8]").value(1))
                    .andExpect(jsonPath("$.monthTotals[9]").value(1))
                    .andExpect(jsonPath("$.professionals[?(@.id == '%s')].total", LAURA).value(contains(2)))
                    .andExpect(jsonPath("$.professionals[?(@.id == '%s')].total", ANDRES).value(contains(0)));
        }

        @Test
        @DisplayName("Un paciente no puede ver el reporte y sin iniciar sesión no hay acceso")
        void reportIsProtected() throws Exception {
            mvc.perform(get("/api/appointments/report/monthly").with(patient(ANA)))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/appointments/report/monthly"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /** Agenda como paciente y devuelve el id de la cita. */
    private String book(String patientId, String professionalId, String date, String time) throws Exception {
        String body = mvc.perform(bookRequest(professionalId, date, time).with(patient(patientId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static MockHttpServletRequestBuilder bookRequest(String professionalId, String date, String time) {
        return post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("""
                        {"professionalId": "%s", "date": "%s", "time": "%s"}
                        """, professionalId, date, time));
    }

    private static MockHttpServletRequestBuilder cancelRequest(String appointmentId, String reason) {
        return post("/api/appointments/mine/{id}/cancel", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("""
                        {"reason": "%s"}
                        """, reason));
    }

    private static String json(String template, Object... values) {
        return template.formatted(values);
    }

    private static RequestPostProcessor patient(String id) {
        return user(id, "ROLE_PATIENT");
    }

    private static RequestPostProcessor professional(String id) {
        return user(id, "ROLE_PROFESSIONAL");
    }

    private static RequestPostProcessor scheduler() {
        return user(UUID.randomUUID().toString(), "ROLE_SCHEDULER");
    }

    // Mismo resultado que un token de Keycloak ya validado: el sub es el id y el rol viene del realm.
    private static RequestPostProcessor user(String id, String role) {
        return jwt().jwt(token -> token.subject(id)).authorities(new SimpleGrantedAuthority(role));
    }
}
