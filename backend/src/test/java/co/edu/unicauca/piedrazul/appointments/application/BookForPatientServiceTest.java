package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.identity.PatientDirectory;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Instant;
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
class BookForPatientServiceTest {

    private static final PatientSummary ANA = new PatientSummary(UUID.randomUUID(), "Ana", "María", "Gómez", "Ruiz",
            "CC", "1061000001", "3001234567");
    private static final LocalTime TEN_AM = LocalTime.of(10, 0);

    @Mock
    private PatientDirectory patientDirectory;
    @Mock
    private BookAppointmentService bookAppointmentService;

    private BookForPatientService service;

    @BeforeEach
    void setUp() {
        service = new BookForPatientService(patientDirectory, bookAppointmentService);
    }

    @Test
    @DisplayName("Agenda la cita a nombre del paciente elegido por el agendador")
    void booksForPatient() {
        BookAppointmentCommand command = command(ANA.id());
        BookedAppointment booked = new BookedAppointment(Appointment.book(UUID.randomUUID(), ANA.id(), LAURA.id(),
                NEXT_WEDNESDAY, TEN_AM, 30, Instant.parse("2026-09-28T14:10:00Z")), LAURA);
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of(ANA));
        when(bookAppointmentService.book(command)).thenReturn(booked);

        StaffBookedAppointment result = service.book(command);

        assertThat(result.booked()).isSameAs(booked);
        assertThat(result.patient()).isEqualTo(ANA);
    }

    @Test
    @DisplayName("No agenda si el paciente no está registrado")
    void rejectsUnknownPatient() {
        UUID unknown = UUID.randomUUID();
        when(patientDirectory.findByIds(List.of(unknown))).thenReturn(List.of());

        assertThatThrownBy(() -> service.book(command(unknown)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No encontramos al paciente seleccionado.");
        verify(bookAppointmentService, never()).book(any());
    }

    @Test
    @DisplayName("Si el paciente ya tiene cita a esa hora, el mensaje le habla al agendador")
    void explainsDoubleBookingToStaff() {
        BookAppointmentCommand command = command(ANA.id());
        when(patientDirectory.findByIds(List.of(ANA.id()))).thenReturn(List.of(ANA));
        when(bookAppointmentService.book(command)).thenThrow(new PatientAlreadyBookedException());

        assertThatThrownBy(() -> service.book(command))
                .isInstanceOf(PatientAlreadyBookedException.class)
                .hasMessage("El paciente ya tiene una cita a esa hora. Elige otro horario.");
    }

    private static BookAppointmentCommand command(UUID patientId) {
        return new BookAppointmentCommand(patientId, LAURA.id(), NEXT_WEDNESDAY, TEN_AM);
    }
}
