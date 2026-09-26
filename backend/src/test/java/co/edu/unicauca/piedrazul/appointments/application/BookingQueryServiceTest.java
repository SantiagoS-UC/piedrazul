package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.ANDRES;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.CLOCK;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.DIANA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAST_BOOKABLE_DATE;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_TUESDAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.NEXT_WEDNESDAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.TODAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.availabilityOf;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.professional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.appointments.domain.Slot;
import co.edu.unicauca.piedrazul.appointments.domain.SlotStatus;
import co.edu.unicauca.piedrazul.configuration.SchedulingSettings;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
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
class BookingQueryServiceTest {

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private SchedulingSettings schedulingSettings;
    @Mock
    private AppointmentRepository appointmentRepository;

    private BookingQueryService service;

    @BeforeEach
    void setUp() {
        BookingSchedules schedules = new BookingSchedules(professionalCatalog, schedulingSettings);
        service = new BookingQueryService(professionalCatalog, schedules, appointmentRepository, CLOCK);
        lenient().when(schedulingSettings.lastBookableDate(TODAY)).thenReturn(LAST_BOOKABLE_DATE);
    }

    @Test
    @DisplayName("Lista solo profesionales activos de la especialidad con disponibilidad, en orden alfabético")
    void listsBookableProfessionals() {
        ProfessionalSummary inactive = professional("Pedro", "Díaz", "GENERAL_MEDICINE", "Medicina general", false);
        ProfessionalSummary withoutSchedule = professional("Ana", "Pérez", "GENERAL_MEDICINE",
                "Medicina general", true);
        // El catálogo ya entrega la lista ordenada por apellido.
        when(professionalCatalog.findAll()).thenReturn(List.of(inactive, LAURA, withoutSchedule, DIANA, ANDRES));
        when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.of(availabilityOf(LAURA)));
        when(schedulingSettings.availabilityOf(ANDRES.id())).thenReturn(Optional.of(availabilityOf(ANDRES)));
        when(schedulingSettings.availabilityOf(withoutSchedule.id())).thenReturn(Optional.empty());

        List<ProfessionalSummary> result = service.professionalsFor("GENERAL_MEDICINE");

        assertThat(result).containsExactly(LAURA, ANDRES);
    }

    @Test
    @DisplayName("El calendario trae los días de atención y el periodo de la ventana")
    void buildsCalendar() {
        givenLauraIsBookable();

        BookingCalendar calendar = service.calendarOf(LAURA.id());

        assertThat(calendar.professional()).isEqualTo(LAURA);
        assertThat(calendar.period().firstDate()).isEqualTo(TODAY);
        assertThat(calendar.period().lastDate()).isEqualTo(LAST_BOOKABLE_DATE);
        assertThat(calendar.schedule().slotMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Las franjas de un día futuro reflejan las citas ya agendadas")
    void listsSlotsWithTakenOnes() {
        givenLauraIsBookable();
        Appointment booked = Appointment.book(UUID.randomUUID(), UUID.randomUUID(), LAURA.id(), NEXT_WEDNESDAY,
                LocalTime.of(10, 0), 30, Instant.parse("2026-09-27T15:00:00Z"));
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), NEXT_WEDNESDAY)).thenReturn(List.of(booked));

        List<Slot> slots = service.slotsOf(LAURA.id(), NEXT_WEDNESDAY);

        assertThat(slots).hasSize(8);
        assertThat(slots).filteredOn(slot -> slot.status() == SlotStatus.TAKEN)
                .extracting(Slot::startTime).containsExactly(LocalTime.of(10, 0));
        assertThat(slots).filteredOn(Slot::available).hasSize(7);
    }

    @Test
    @DisplayName("Hoy, las franjas anteriores a la hora actual aparecen como pasadas")
    void marksPastSlotsToday() {
        givenLauraIsBookable();
        when(appointmentRepository.findByProfessionalAndDate(LAURA.id(), TODAY)).thenReturn(List.of());

        List<Slot> slots = service.slotsOf(LAURA.id(), TODAY);

        // Son las 9:10: 8:00, 8:30 y 9:00 ya pasaron.
        assertThat(slots).filteredOn(slot -> slot.status() == SlotStatus.PAST).hasSize(3);
        assertThat(slots.get(3).startTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(slots.get(3).available()).isTrue();
    }

    @Test
    @DisplayName("No da franjas para un día que el profesional no atiende")
    void rejectsNonWorkingDay() {
        givenLauraIsBookable();

        assertThatThrownBy(() -> service.slotsOf(LAURA.id(), NEXT_TUESDAY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Elige una fecha disponible del calendario.");
    }

    @Test
    @DisplayName("No da franjas fuera de la ventana de agendamiento")
    void rejectsDateOutsideWindow() {
        givenLauraIsBookable();

        assertThatThrownBy(() -> service.slotsOf(LAURA.id(), LAST_BOOKABLE_DATE.plusDays(2)))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.slotsOf(LAURA.id(), TODAY.minusDays(5)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private void givenLauraIsBookable() {
        when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));
        when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.of(availabilityOf(LAURA)));
        lenient().when(appointmentRepository.findByProfessionalAndDate(any(), any())).thenReturn(List.of());
    }
}
