package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAST_BOOKABLE_DATE;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.TODAY;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.availabilityOf;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.professional;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.BookingPeriod;
import co.edu.unicauca.piedrazul.appointments.domain.ProfessionalSchedule;
import co.edu.unicauca.piedrazul.configuration.SchedulingSettings;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import co.edu.unicauca.piedrazul.shared.domain.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingSchedulesTest {

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private SchedulingSettings schedulingSettings;

    private BookingSchedules schedules;

    @BeforeEach
    void setUp() {
        schedules = new BookingSchedules(professionalCatalog, schedulingSettings);
    }

    @Test
    @DisplayName("Devuelve el profesional si existe y está activo")
    void findsActiveProfessional() {
        when(professionalCatalog.findById(LAURA.id())).thenReturn(Optional.of(LAURA));

        assertThat(schedules.activeProfessional(LAURA.id())).isEqualTo(LAURA);
    }

    @Test
    @DisplayName("Trata a un profesional inactivo como inexistente")
    void rejectsInactiveProfessional() {
        ProfessionalSummary inactive = professional("Pedro", "Díaz", "GENERAL_MEDICINE", "Medicina general", false);
        when(professionalCatalog.findById(inactive.id())).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> schedules.activeProfessional(inactive.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Falla si el profesional no existe")
    void rejectsUnknownProfessional() {
        UUID unknown = UUID.randomUUID();
        when(professionalCatalog.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schedules.activeProfessional(unknown))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El profesional seleccionado no existe.");
    }

    @Test
    @DisplayName("Traduce la disponibilidad configurada al horario del agendamiento")
    void translatesAvailability() {
        when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.of(availabilityOf(LAURA)));

        ProfessionalSchedule schedule = schedules.scheduleOf(LAURA.id());

        assertThat(schedule.workingDays()).containsExactlyInAnyOrder(MONDAY, WEDNESDAY);
        assertThat(schedule.slotStartTimes()).hasSize(8);
        assertThat(schedule.slotMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Falla si el profesional no tiene disponibilidad configurada")
    void rejectsProfessionalWithoutSchedule() {
        when(schedulingSettings.availabilityOf(LAURA.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schedules.scheduleOf(LAURA.id()))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(schedules.hasSchedule(LAURA.id())).isFalse();
    }

    @Test
    @DisplayName("El periodo va de hoy hasta el final de la ventana de agendamiento")
    void buildsPeriodFromWindow() {
        when(schedulingSettings.lastBookableDate(TODAY)).thenReturn(LAST_BOOKABLE_DATE);

        assertThat(schedules.periodStarting(TODAY)).isEqualTo(new BookingPeriod(TODAY, LAST_BOOKABLE_DATE));
    }
}
