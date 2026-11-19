package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.CLOCK;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.DIANA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.TODAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientHistoryQueryServiceTest {

    private static final PatientSummary ANA = new PatientSummary(UUID.randomUUID(), "Ana", "María", "Gómez", "Ruiz",
            "CC", "1061000001", "3001234567");

    @Mock
    private PatientDirectory patientDirectory;
    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private AppointmentRepository appointmentRepository;

    private PatientHistoryQueryService service;

    @BeforeEach
    void setUp() {
        service = new PatientHistoryQueryService(patientDirectory, professionalCatalog, appointmentRepository, CLOCK);
    }

    @Test
    @DisplayName("Reúne las citas del paciente con todos los profesionales y distingue próximas y pasadas")
    void buildsHistoryWithAllProfessionals() {
        // Hoy es el lunes 28 de septiembre de 2026 a las 9:10 a. m.
        Appointment upcoming = appointmentWith(DIANA.id(), TODAY.plusDays(2), LocalTime.of(8, 0));
        Appointment earlierToday = appointmentWith(LAURA.id(), TODAY, LocalTime.of(8, 0));
        Appointment lastMonth = appointmentWith(LAURA.id(), TODAY.minusMonths(1), LocalTime.of(10, 0));
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of(ANA));
        when(appointmentRepository.findByPatient(ANA.id())).thenReturn(List.of(upcoming, earlierToday, lastMonth));
        when(professionalCatalog.findAll()).thenReturn(List.of(DIANA, LAURA));

        PatientHistory history = service.historyOf(ANA.id());

        assertThat(history.patient()).isEqualTo(ANA);
        assertThat(history.entries()).extracting(PatientHistoryEntry::appointment)
                .containsExactly(upcoming, earlierToday, lastMonth);
        assertThat(history.entries()).extracting(PatientHistoryEntry::upcoming).containsExactly(true, false, false);
        assertThat(history.entries().get(0).professional()).contains(DIANA);
        assertThat(history.total()).isEqualTo(3);
        assertThat(history.upcoming()).isEqualTo(1);
        assertThat(history.past()).isEqualTo(2);
    }

    @Test
    @DisplayName("Devuelve un historial vacío si el paciente aún no tiene citas")
    void emptyHistory() {
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of(ANA));
        when(appointmentRepository.findByPatient(ANA.id())).thenReturn(List.of());
        when(professionalCatalog.findAll()).thenReturn(List.of(LAURA));

        PatientHistory history = service.historyOf(ANA.id());

        assertThat(history.total()).isZero();
        assertThat(history.upcoming()).isZero();
        assertThat(history.past()).isZero();
    }

    @Test
    @DisplayName("Conserva la cita aunque el profesional ya no esté en el catálogo")
    void keepsAppointmentWithUnknownProfessional() {
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of(ANA));
        when(appointmentRepository.findByPatient(ANA.id()))
                .thenReturn(List.of(appointmentWith(UUID.randomUUID(), TODAY.minusDays(3), LocalTime.of(9, 0))));
        when(professionalCatalog.findAll()).thenReturn(List.of(LAURA));

        PatientHistory history = service.historyOf(ANA.id());

        assertThat(history.total()).isEqualTo(1);
        assertThat(history.entries().get(0).professional()).isEmpty();
    }

    @Test
    @DisplayName("Falla si el paciente no existe")
    void rejectsUnknownPatient() {
        UUID unknown = UUID.randomUUID();
        when(patientDirectory.findByIds(List.of(unknown))).thenReturn(List.of());

        assertThatThrownBy(() -> service.historyOf(unknown)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(appointmentRepository, professionalCatalog);
    }

    private static Appointment appointmentWith(UUID professionalId, LocalDate date, LocalTime start) {
        return Appointment.book(UUID.randomUUID(), ANA.id(), professionalId, date, start, 20,
                Instant.parse("2026-08-01T15:00:00Z"));
    }
}
