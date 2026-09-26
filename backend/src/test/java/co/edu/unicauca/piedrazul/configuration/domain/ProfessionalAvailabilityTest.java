package co.edu.unicauca.piedrazul.configuration.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfessionalAvailabilityTest {

    private static final UUID PROFESSIONAL = UUID.randomUUID();
    private static final Set<DayOfWeek> MON_WED_FRI = Set.of(MONDAY, WEDNESDAY, FRIDAY);

    @Test
    @DisplayName("Genera las franjas desde la hora de inicio separadas por el intervalo")
    void generatesSlots() {
        ProfessionalAvailability availability = availability(time(8, 0), time(10, 0), 30);

        assertThat(availability.slotStartTimes())
                .containsExactly(time(8, 0), time(8, 30), time(9, 0), time(9, 30));
    }

    @Test
    @DisplayName("Solo ofrece una franja si cabe completa antes de la hora de fin")
    void lastSlotMustFitBeforeEndTime() {
        ProfessionalAvailability availability = availability(time(8, 0), time(12, 0), 45);

        assertThat(availability.slotStartTimes())
                .containsExactly(time(8, 0), time(8, 45), time(9, 30), time(10, 15), time(11, 0));
    }

    @Test
    @DisplayName("No da la vuelta a medianoche cuando la franja termina tarde")
    void doesNotWrapAroundMidnight() {
        ProfessionalAvailability availability = availability(time(22, 0), time(23, 59), 60);

        assertThat(availability.slotStartTimes()).containsExactly(time(22, 0));
    }

    @Test
    @DisplayName("Sabe qué días atiende el profesional")
    void worksOnConfiguredDays() {
        ProfessionalAvailability availability = availability(time(8, 0), time(12, 0), 30);

        assertThat(availability.worksOn(MONDAY)).isTrue();
        assertThat(availability.worksOn(DayOfWeek.TUESDAY)).isFalse();
    }

    @Test
    @DisplayName("Exige al menos un día de atención")
    void requiresWorkingDays() {
        assertThatThrownBy(() -> new ProfessionalAvailability(PROFESSIONAL, Set.of(), time(8, 0), time(12, 0), 30))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Selecciona al menos un día de atención.");
    }

    @Test
    @DisplayName("Solo permite días de lunes a sábado")
    void rejectsSunday() {
        assertThatThrownBy(() -> new ProfessionalAvailability(PROFESSIONAL, Set.of(SATURDAY, SUNDAY), time(8, 0),
                time(12, 0), 30))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Los días de atención van de lunes a sábado.");
    }

    @Test
    @DisplayName("Exige las horas de inicio y de fin")
    void requiresTimes() {
        assertThatThrownBy(() -> availability(null, time(12, 0), 30))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Escribe la hora de inicio.");
        assertThatThrownBy(() -> availability(time(8, 0), null, 30))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Escribe la hora de fin.");
    }

    @Test
    @DisplayName("La hora de fin debe ser posterior a la de inicio")
    void endMustBeAfterStart() {
        assertThatThrownBy(() -> availability(time(12, 0), time(12, 0), 30))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La hora de fin debe ser posterior a la hora de inicio.");
    }

    @Test
    @DisplayName("El intervalo debe estar entre 10 y 120 minutos")
    void slotMinutesRange() {
        assertThatThrownBy(() -> availability(time(8, 0), time(12, 0), 5))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El intervalo debe estar entre 10 y 120 minutos.");
        assertThatThrownBy(() -> availability(time(8, 0), time(12, 0), 121))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("El intervalo no puede ser mayor que la franja horaria")
    void slotMustFitInRange() {
        assertThatThrownBy(() -> availability(time(8, 0), time(8, 30), 45))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El intervalo no puede ser mayor que la franja horaria.");
    }

    @Test
    @DisplayName("Ignora los segundos de las horas recibidas")
    void truncatesSeconds() {
        ProfessionalAvailability availability = availability(LocalTime.of(8, 0, 30), time(9, 0), 30);

        assertThat(availability.startTime()).isEqualTo(time(8, 0));
    }

    private static ProfessionalAvailability availability(LocalTime start, LocalTime end, int slotMinutes) {
        return new ProfessionalAvailability(PROFESSIONAL, MON_WED_FRI, start, end, slotMinutes);
    }

    private static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
