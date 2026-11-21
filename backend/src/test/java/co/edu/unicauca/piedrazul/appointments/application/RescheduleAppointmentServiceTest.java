package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.ANDRES;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.CLOCK;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAST_BOOKABLE_DATE;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_TUESDAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.TODAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.availabilityOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.PatientAlreadyBookedException;
import co.edu.unicauca.piedrazul.appointments.domain.SlotTakenException;
import co.edu.unicauca.piedrazul.configuration.SchedulingSettings;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class RescheduleAppointmentServiceTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-09-20T15:00:00Z");
    private static final LocalTime TEN_AM = LocalTime.of(10, 0);
    private static final LocalTime ELEVEN_AM = LocalTime.of(11, 0);
    private static final LocalDate NEXT_MONDAY = TODAY.plusWeeks(1);

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private SchedulingSettings schedulingSettings;
    @Mock
    private AppointmentRepository appointmentRepository;

    private RescheduleAppointmentService service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        BookingSchedules schedules = new BookingSchedules(professionalCatalog, schedulingSettings);
        service = new RescheduleAppointmentService(schedules, appointmentRepository, CLOCK);
        appointment = Appointment.book(UUID.randomUUID(), PATIENT_ID, LAURA.id(), NEXT_WEDNESDAY, TEN_AM, 30,
                CREATED_AT);
        lenient().when(appointmentRepository.findById(appointment.id())).thenReturn(Optional.of(appointment));
        lenient().when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        lenient().when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.of(availabilityOf(LAURA)));
        lenient().when(schedulingSettings.lastBookableDate(TODAY)).thenReturn(LAST_BOOKABLE_DATE);
        lenient().when(appointmentRepository.findByPatientAndDate(any(), any())).thenReturn(List.of());
        lenient().when(appointmentRepository.findByProfessionalAndDate(any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Mueve la cita a otra fecha y hora sin cambiar su id, su paciente ni su creación")
    void reschedulesAppointment() {
        BookedAppointment result = service.reschedule(laura(), command(NEXT_MONDAY, ELEVEN_AM));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        Appointment saved = captor.getValue();
        assertThat(saved.id()).isEqualTo(appointment.id());
        assertThat(saved.patientId()).isEqualTo(PATIENT_ID);
        assertThat(saved.professionalId()).isEqualTo(LAURA.id());
        assertThat(saved.date()).isEqualTo(NEXT_MONDAY);
        assertThat(saved.startTime()).isEqualTo(ELEVEN_AM);
        assertThat(saved.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.professional()).isEqualTo(LAURA);
    }

    @Test
    @DisplayName("Puede moverla a la franja siguiente del mismo día aunque hoy se cruce con su hora actual")
    void ignoresItselfWhenCheckingConflicts() {
        // Se agendó cuando las citas duraban 45 minutos: 10:00 a 10:45 se cruza con la franja de 10:30.
        Appointment longer = Appointment.book(UUID.randomUUID(), PATIENT_ID, LAURA.id(), NEXT_WEDNESDAY, TEN_AM, 45,
                CREATED_AT);
        when(appointmentRepository.findById(longer.id())).thenReturn(Optional.of(longer));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY)).thenReturn(List.of(longer));
        when(appointmentRepository.findByPatientAndDate(PATIENT_ID, NEXT_WEDNESDAY)).thenReturn(List.of(longer));

        service.reschedule(laura(),
                new RescheduleAppointmentCommand(longer.id(), NEXT_WEDNESDAY, LocalTime.of(10, 30)));

        verify(appointmentRepository).save(any());
    }

    @Test
    @DisplayName("El agendador puede reagendar citas de cualquier profesional")
    void schedulerReschedulesAnyAgenda() {
        service.reschedule(Requester.scheduler(UUID.randomUUID()), command(NEXT_MONDAY, ELEVEN_AM));

        verify(appointmentRepository).save(any());
    }

    @Test
    @DisplayName("Un profesional no puede reagendar citas de otro")
    void rejectsOtherProfessional() {
        assertThatThrownBy(() -> service.reschedule(Requester.professional(ANDRES.id()), command(NEXT_MONDAY,
                ELEVEN_AM)))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessage("Solo puedes reagendar citas de tu propia agenda.");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Avisa si la cita no existe")
    void rejectsUnknownAppointment() {
        UUID unknown = UUID.randomUUID();
        when(appointmentRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reschedule(laura(),
                new RescheduleAppointmentCommand(unknown, NEXT_MONDAY, ELEVEN_AM)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("No reagenda una cita que ya empezó")
    void rejectsPastAppointment() {
        Appointment past = Appointment.book(UUID.randomUUID(), PATIENT_ID, LAURA.id(), TODAY, LocalTime.of(8, 0), 30,
                CREATED_AT);
        when(appointmentRepository.findById(past.id())).thenReturn(Optional.of(past));

        assertThatThrownBy(() -> service.reschedule(laura(),
                new RescheduleAppointmentCommand(past.id(), NEXT_MONDAY, ELEVEN_AM)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Solo se pueden reagendar citas que aún no han empezado.");
    }

    @Test
    @DisplayName("No reagenda una cita cancelada")
    void rejectsCancelledAppointment() {
        Appointment cancelled = appointment.cancel("Tengo un viaje de trabajo", CREATED_AT, TODAY);
        when(appointmentRepository.findById(cancelled.id())).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> service.reschedule(laura(), command(NEXT_MONDAY, ELEVEN_AM)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Esta cita fue cancelada y ya no se puede reagendar.");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pide una fecha u hora distinta a la actual")
    void rejectsSameSlot() {
        assertRejected(command(NEXT_WEDNESDAY, TEN_AM), "La cita ya está en ese horario. Elige otra fecha u hora.");
    }

    @Test
    @DisplayName("Aplica las reglas del calendario: días de atención y horas de las franjas")
    void appliesCalendarRules() {
        assertRejected(command(NEXT_TUESDAY, TEN_AM), "Elige una fecha disponible del calendario.");
        assertRejected(command(NEXT_MONDAY, LocalTime.of(10, 15)), "Elige una de las horas disponibles.");
    }

    @Test
    @DisplayName("No la mueve a una franja ocupada por otro paciente")
    void rejectsTakenSlot() {
        Appointment other = Appointment.book(UUID.randomUUID(), UUID.randomUUID(), LAURA.id(), NEXT_MONDAY,
                ELEVEN_AM, 30, CREATED_AT);
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_MONDAY)).thenReturn(List.of(other));

        assertThatThrownBy(() -> service.reschedule(laura(), command(NEXT_MONDAY, ELEVEN_AM)))
                .isInstanceOf(SlotTakenException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("No la mueve a una hora en la que el paciente tiene otra cita")
    void rejectsPatientConflict() {
        Appointment withAndres = Appointment.book(UUID.randomUUID(), PATIENT_ID, ANDRES.id(), NEXT_MONDAY,
                ELEVEN_AM, 30, CREATED_AT);
        when(appointmentRepository.findByPatientAndDate(PATIENT_ID, NEXT_MONDAY)).thenReturn(List.of(withAndres));

        assertThatThrownBy(() -> service.reschedule(laura(), command(NEXT_MONDAY, ELEVEN_AM)))
                .isInstanceOf(PatientAlreadyBookedException.class)
                .hasMessage("El paciente ya tiene una cita a esa hora. Elige otro horario.");
        verify(appointmentRepository, never()).save(any());
    }

    private void assertRejected(RescheduleAppointmentCommand command, String message) {
        assertThatThrownBy(() -> service.reschedule(laura(), command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage(message);
        verify(appointmentRepository, never()).save(any());
    }

    private RescheduleAppointmentCommand command(LocalDate date, LocalTime time) {
        return new RescheduleAppointmentCommand(appointment.id(), date, time);
    }

    private static Requester laura() {
        return Requester.professional(LAURA.id());
    }
}
