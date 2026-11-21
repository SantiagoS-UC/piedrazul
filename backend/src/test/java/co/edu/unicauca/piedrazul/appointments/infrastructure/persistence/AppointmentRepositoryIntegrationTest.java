package co.edu.unicauca.piedrazul.appointments.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.IntegrationTestConfiguration;
import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentStatus;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Prueba de integración del adaptador de persistencia con PostgreSQL real: comprueba lo que las
 * pruebas unitarias no pueden, como las migraciones, los índices únicos parciales y la traducción de
 * sus errores a excepciones del dominio.
 */
@SpringBootTest
@Import(IntegrationTestConfiguration.class)
class AppointmentRepositoryIntegrationTest {

    // Datos semilla de las migraciones de identity y professionals.
    private static final UUID ANA = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID CARLOS = UUID.fromString("00000000-0000-0000-0000-000000000102");
    private static final UUID LAURA = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ANDRES = UUID.fromString("20000000-0000-0000-0000-000000000002");

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 9, 30);
    private static final LocalTime TEN_AM = LocalTime.of(10, 0);

    @Autowired
    private AppointmentRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanAppointments() {
        jdbcTemplate.update("DELETE FROM appointments");
    }

    @Test
    @DisplayName("Guarda una cita y la recupera con todos sus datos")
    void savesAndRestores() {
        Appointment appointment = appointment(ANA, LAURA, WEDNESDAY, TEN_AM);

        repository.save(appointment);

        Appointment restored = repository.findById(appointment.id()).orElseThrow();
        assertThat(restored.patientId()).isEqualTo(ANA);
        assertThat(restored.professionalId()).isEqualTo(LAURA);
        assertThat(restored.date()).isEqualTo(WEDNESDAY);
        assertThat(restored.startTime()).isEqualTo(TEN_AM);
        assertThat(restored.durationMinutes()).isEqualTo(20);
        assertThat(restored.status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("La base de datos impide dos citas del mismo profesional en la misma franja")
    void rejectsTakenSlot() {
        repository.save(appointment(ANA, LAURA, WEDNESDAY, TEN_AM));

        assertThatThrownBy(() -> repository.save(appointment(CARLOS, LAURA, WEDNESDAY, TEN_AM)))
                .isInstanceOf(SlotTakenException.class);
    }

    @Test
    @DisplayName("La base de datos impide que un paciente tenga dos citas a la misma hora")
    void rejectsPatientDoubleBooking() {
        repository.save(appointment(ANA, LAURA, WEDNESDAY, TEN_AM));

        assertThatThrownBy(() -> repository.save(appointment(ANA, ANDRES, WEDNESDAY, TEN_AM)))
                .isInstanceOf(PatientAlreadyBookedException.class);
    }

    @Test
    @DisplayName("Una cita cancelada sigue en el historial pero libera la franja para otro paciente")
    void cancelledAppointmentFreesSlot() {
        Appointment first = appointment(ANA, LAURA, WEDNESDAY, TEN_AM);
        repository.save(first);
        repository.save(first.cancel("Tengo un viaje de trabajo", IntegrationTestConfiguration.NOW, TODAY));

        Appointment second = appointment(CARLOS, LAURA, WEDNESDAY, TEN_AM);
        repository.save(second);

        assertThat(repository.findByProfessionalAndDate(LAURA, WEDNESDAY))
                .extracting(Appointment::id)
                .containsExactly(second.id());
        assertThat(repository.findByPatient(ANA)).hasSize(1);
        Appointment cancelled = repository.findById(first.id()).orElseThrow();
        assertThat(cancelled.isCancelled()).isTrue();
        assertThat(cancelled.cancellation().orElseThrow().reason()).isEqualTo("Tengo un viaje de trabajo");
    }

    @Test
    @DisplayName("Al reagendar actualiza la misma fila en lugar de crear otra")
    void reschedulesInPlace() {
        Appointment appointment = appointment(ANA, LAURA, WEDNESDAY, TEN_AM);
        repository.save(appointment);

        repository.save(appointment.rescheduleTo(WEDNESDAY.plusDays(1), LocalTime.of(8, 0), 20));

        assertThat(repository.findByPatient(ANA)).hasSize(1);
        assertThat(repository.findById(appointment.id()).orElseThrow().date()).isEqualTo(WEDNESDAY.plusDays(1));
        assertThat(repository.findByProfessionalAndDate(LAURA, WEDNESDAY)).isEmpty();
    }

    @Test
    @DisplayName("Las consultas del reporte solo tienen en cuenta las citas vigentes")
    void reportQueriesIgnoreCancelled() {
        Appointment september = appointment(ANA, LAURA, WEDNESDAY, TEN_AM);
        Appointment october = appointment(CARLOS, ANDRES, LocalDate.of(2026, 10, 14), LocalTime.of(14, 0));
        Appointment november = appointment(ANA, ANDRES, LocalDate.of(2026, 11, 4), LocalTime.of(14, 0));
        repository.save(september);
        repository.save(october);
        repository.save(november);
        repository.save(november.cancel("Ya no necesito la consulta", IntegrationTestConfiguration.NOW, TODAY));

        assertThat(repository.findScheduledBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .extracting(Appointment::id)
                .containsExactlyInAnyOrder(september.id(), october.id());
        assertThat(repository.firstScheduledDate()).contains(WEDNESDAY);
        assertThat(repository.lastScheduledDate()).contains(LocalDate.of(2026, 10, 14));
    }

    private static Appointment appointment(UUID patientId, UUID professionalId, LocalDate date, LocalTime time) {
        return Appointment.book(UUID.randomUUID(), patientId, professionalId, date, time, 20,
                IntegrationTestConfiguration.NOW);
    }
}
