package co.edu.unicauca.piedrazul.appointments.application;

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
import static org.mockito.Mockito.doThrow;
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
class BookAppointmentServiceTest {

    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final LocalTime TEN_AM = LocalTime.of(10, 0);

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private SchedulingSettings schedulingSettings;
    @Mock
    private AppointmentRepository appointmentRepository;

    private BookAppointmentService service;

    @BeforeEach
    void setUp() {
        BookingSchedules schedules = new BookingSchedules(professionalCatalog, schedulingSettings);
        service = new BookAppointmentService(schedules, appointmentRepository, CLOCK);
        lenient().when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        lenient().when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.of(availabilityOf(LAURA)));
        lenient().when(schedulingSettings.lastBookableDate(TODAY)).thenReturn(LAST_BOOKABLE_DATE);
        lenient().when(appointmentRepository.findByPatientAndDate(any(), any())).thenReturn(List.of());
        lenient().when(appointmentRepository.findByProfessionalAndDate(any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Registra la cita con la duración vigente del profesional")
    void booksAppointment() {
        BookedAppointment booked = service.book(command(NEXT_WEDNESDAY, TEN_AM));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        Appointment saved = captor.getValue();
        assertThat(saved.patientId()).isEqualTo(PATIENT_ID);
        assertThat(saved.professionalId()).isEqualTo(LAURA.id());
        assertThat(saved.date()).isEqualTo(NEXT_WEDNESDAY);
        assertThat(saved.startTime()).isEqualTo(TEN_AM);
        assertThat(saved.durationMinutes()).isEqualTo(30);
        assertThat(saved.createdAt()).isEqualTo(CLOCK.instant());
        assertThat(booked.appointment()).isSameAs(saved);
        assertThat(booked.professional()).isEqualTo(LAURA);
    }

    @Test
    @DisplayName("Permite agendar hoy en una franja que aún no pasó")
    void booksLaterToday() {
        service.book(command(TODAY, LocalTime.of(9, 30)));

        verify(appointmentRepository).save(any());
    }

    @Test
    @DisplayName("No agenda con un profesional que no existe")
    void rejectsUnknownProfessional() {
        UUID unknown = UUID.randomUUID();
        when(professionalCatalog.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.book(new BookAppointmentCommand(PATIENT_ID, unknown, NEXT_WEDNESDAY, TEN_AM)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("No agenda en un día que el profesional no atiende")
    void rejectsNonWorkingDay() {
        assertRejected(command(NEXT_TUESDAY, TEN_AM), "Elige una fecha disponible del calendario.");
    }

    @Test
    @DisplayName("No agenda fuera de la ventana de agendamiento")
    void rejectsDateOutsideWindow() {
        LocalDate farAway = LAST_BOOKABLE_DATE.plusWeeks(1).plusDays(2);

        assertRejected(command(farAway, TEN_AM), "Elige una fecha disponible del calendario.");
    }

    @Test
    @DisplayName("No agenda a una hora que no es inicio de franja")
    void rejectsTimeOutsideSlots() {
        assertRejected(command(NEXT_WEDNESDAY, LocalTime.of(10, 15)), "Elige una de las horas disponibles.");
        assertRejected(command(NEXT_WEDNESDAY, LocalTime.of(12, 0)), "Elige una de las horas disponibles.");
    }

    @Test
    @DisplayName("No agenda en una franja de hoy que ya pasó")
    void rejectsPastSlotToday() {
        assertRejected(command(TODAY, LocalTime.of(9, 0)), "Esa hora ya pasó. Elige una hora más tarde.");
    }

    @Test
    @DisplayName("No agenda si el paciente ya tiene una cita a esa hora, aunque sea con otro profesional")
    void rejectsPatientDoubleBooking() {
        Appointment own = Appointment.book(UUID.randomUUID(), PATIENT_ID, UUID.randomUUID(), NEXT_WEDNESDAY, TEN_AM,
                20, Instant.parse("2026-09-27T15:00:00Z"));
        when(appointmentRepository.findByPatientAndDate(PATIENT_ID, NEXT_WEDNESDAY)).thenReturn(List.of(own));

        assertThatThrownBy(() -> service.book(command(NEXT_WEDNESDAY, TEN_AM)))
                .isInstanceOf(PatientAlreadyBookedException.class)
                .hasMessage("Ya tienes una cita a esa hora. Elige otro horario.");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("No agenda una franja que otro paciente ya reservó")
    void rejectsTakenSlot() {
        Appointment other = Appointment.book(UUID.randomUUID(), UUID.randomUUID(), LAURA.id(), NEXT_WEDNESDAY,
                TEN_AM, 30, Instant.parse("2026-09-27T15:00:00Z"));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY)).thenReturn(List.of(other));

        assertThatThrownBy(() -> service.book(command(NEXT_WEDNESDAY, TEN_AM)))
                .isInstanceOf(SlotTakenException.class)
                .hasMessage("Ese horario acaba de ser tomado por otra persona. Elige otra hora.");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si otro paciente confirma al mismo tiempo, el error del repositorio llega al usuario")
    void propagatesConcurrentBooking() {
        doThrow(new SlotTakenException()).when(appointmentRepository).save(any());

        assertThatThrownBy(() -> service.book(command(NEXT_WEDNESDAY, TEN_AM)))
                .isInstanceOf(SlotTakenException.class);
    }

    private void assertRejected(BookAppointmentCommand command, String message) {
        assertThatThrownBy(() -> service.book(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage(message);
        verify(appointmentRepository, never()).save(any());
    }

    private static BookAppointmentCommand command(LocalDate date, LocalTime time) {
        return new BookAppointmentCommand(PATIENT_ID, LAURA.id(), date, time);
    }
}
