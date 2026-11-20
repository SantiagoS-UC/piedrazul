package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.CLOCK;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_TUESDAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentServiceTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final String REASON = "Tengo un viaje de trabajo";

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ProfessionalCatalog professionalCatalog;

    private CancelAppointmentService service;

    @BeforeEach
    void setUp() {
        service = new CancelAppointmentService(appointmentRepository, professionalCatalog, CLOCK);
        lenient().when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
    }

    @Test
    @DisplayName("El paciente cancela su cita con dos días de anticipación y queda guardada como cancelada")
    void cancelsOwnAppointment() {
        Appointment appointment = appointmentOn(NEXT_WEDNESDAY, PATIENT_ID);

        PatientHistoryEntry result = service.cancel(new CancelAppointmentCommand(PATIENT_ID, appointment.id(),
                REASON));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().isCancelled()).isTrue();
        assertThat(captor.getValue().cancellation().orElseThrow().reason()).isEqualTo(REASON);
        assertThat(captor.getValue().cancellation().orElseThrow().cancelledAt()).isEqualTo(CLOCK.instant());
        assertThat(result.professional()).contains(LAURA);
        assertThat(result.cancellable()).isFalse();
    }

    @Test
    @DisplayName("No cancela una cita de mañana")
    void rejectsLessThanTwoDays() {
        Appointment appointment = appointmentOn(NEXT_TUESDAY, PATIENT_ID);

        assertThatThrownBy(() -> service.cancel(new CancelAppointmentCommand(PATIENT_ID, appointment.id(), REASON)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un paciente no puede cancelar la cita de otro")
    void rejectsOtherPatient() {
        Appointment appointment = appointmentOn(NEXT_WEDNESDAY, UUID.randomUUID());

        assertThatThrownBy(() -> service.cancel(new CancelAppointmentCommand(PATIENT_ID, appointment.id(), REASON)))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessage("Solo puedes cancelar tus propias citas.");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Avisa si la cita no existe")
    void rejectsUnknownAppointment() {
        UUID unknown = UUID.randomUUID();
        when(appointmentRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(new CancelAppointmentCommand(PATIENT_ID, unknown, REASON)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Appointment appointmentOn(LocalDate date, UUID patientId) {
        Appointment appointment = Appointment.book(UUID.randomUUID(), patientId, LAURA.id(), date,
                LocalTime.of(10, 0), 30, Instant.parse("2026-09-20T15:00:00Z"));
        when(appointmentRepository.findById(appointment.id())).thenReturn(Optional.of(appointment));
        return appointment;
    }
}
