package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendaQueryServiceTest {

    private static final PatientSummary ANA = new PatientSummary(UUID.randomUUID(), "Ana", "María", "Gómez", "Ruiz",
            "CC", "1061000001", "3001234567");
    private static final PatientSummary CARLOS = new PatientSummary(UUID.randomUUID(), "Carlos", null, "Muñoz",
            null, "CC", "1061000002", "3009876543");

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private PatientDirectory patientDirectory;
    @Mock
    private AppointmentRepository appointmentRepository;

    private AgendaQueryService service;

    @BeforeEach
    void setUp() {
        service = new AgendaQueryService(professionalCatalog, patientDirectory, appointmentRepository);
    }

    @Test
    @DisplayName("Lista las citas del día ordenadas por hora, con los datos de cada paciente")
    void listsAppointmentsByTime() {
        Appointment late = appointmentOf(CARLOS, LocalTime.of(10, 0));
        Appointment early = appointmentOf(ANA, LocalTime.of(8, 0));
        when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY))
                .thenReturn(List.of(late, early));
        when(patientDirectory.findByIds(List.of(ANA.id(), CARLOS.id()))).thenReturn(List.of(CARLOS, ANA));

        ProfessionalAgenda agenda = service.agendaOf(LAURA.id(), NEXT_WEDNESDAY);

        assertThat(agenda.professional()).isEqualTo(LAURA);
        assertThat(agenda.date()).isEqualTo(NEXT_WEDNESDAY);
        assertThat(agenda.total()).isEqualTo(2);
        assertThat(agenda.entries()).extracting(entry -> entry.appointment().startTime())
                .containsExactly(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(agenda.entries()).extracting(entry -> entry.patient().orElseThrow())
                .containsExactly(ANA, CARLOS);
    }

    @Test
    @DisplayName("Responde con cero citas si el profesional no tiene citas ese día")
    void emptyAgenda() {
        when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY)).thenReturn(List.of());
        when(patientDirectory.findByIds(List.of())).thenReturn(List.of());

        ProfessionalAgenda agenda = service.agendaOf(LAURA.id(), NEXT_WEDNESDAY);

        assertThat(agenda.total()).isZero();
        assertThat(agenda.entries()).isEmpty();
    }

    @Test
    @DisplayName("Conserva la cita aunque no se encuentren los datos del paciente")
    void keepsAppointmentWithoutPatient() {
        when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY))
                .thenReturn(List.of(appointmentOf(ANA, LocalTime.of(8, 0))));
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of());

        ProfessionalAgenda agenda = service.agendaOf(LAURA.id(), NEXT_WEDNESDAY);

        assertThat(agenda.total()).isEqualTo(1);
        assertThat(agenda.entries().get(0).patient()).isEmpty();
    }

    @Test
    @DisplayName("Falla si el profesional no existe")
    void rejectsUnknownProfessional() {
        UUID unknown = UUID.randomUUID();
        when(professionalCatalog.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendaOf(unknown, NEXT_WEDNESDAY))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Appointment appointmentOf(PatientSummary patient, LocalTime start) {
        return Appointment.book(UUID.randomUUID(), patient.id(), LAURA.id(), NEXT_WEDNESDAY, start, 20,
                Instant.parse("2026-09-27T15:00:00Z"));
    }
}
