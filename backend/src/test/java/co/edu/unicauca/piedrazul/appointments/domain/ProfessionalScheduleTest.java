package co.edu.unicauca.piedrazul.appointments.domain;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfessionalScheduleTest {

    private static final LocalDate MONDAY_DATE = LocalDate.of(2026, 9, 28);
    private static final LocalDate TUESDAY_DATE = MONDAY_DATE.plusDays(1);
    private static final LocalDateTime SUNDAY_NIGHT = LocalDateTime.of(2026, 9, 27, 20, 0);

    // Lunes y miércoles de 8:00 a 10:00, cada 30 minutos.
    private final ProfessionalSchedule schedule = new ProfessionalSchedule(Set.of(MONDAY, WEDNESDAY),
            List.of(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30)), 30);

    @Test
    @DisplayName("Sabe qué días atiende el profesional")
    void knowsWorkingDays() {
        assertThat(schedule.worksOn(MONDAY_DATE)).isTrue();
        assertThat(schedule.worksOn(TUESDAY_DATE)).isFalse();
    }

    @Test
    @DisplayName("Solo ofrece las horas de inicio de sus franjas")
    void offersOnlySlotStarts() {
        assertThat(schedule.offersSlotAt(LocalTime.of(8, 30))).isTrue();
        assertThat(schedule.offersSlotAt(LocalTime.of(8, 15))).isFalse();
        assertThat(schedule.offersSlotAt(LocalTime.of(10, 0))).isFalse();
    }

    @Test
    @DisplayName("Todas las franjas están libres si no hay citas")
    void allSlotsAvailable() {
        List<Slot> slots = schedule.slotsOn(MONDAY_DATE, List.of(), SUNDAY_NIGHT);

        assertThat(slots).hasSize(4).allMatch(Slot::available);
    }

    @Test
    @DisplayName("Marca como ocupadas las franjas con cita")
    void marksTakenSlots() {
        List<Slot> slots = schedule.slotsOn(MONDAY_DATE, List.of(appointmentAt(LocalTime.of(8, 30), 30)),
                SUNDAY_NIGHT);

        assertThat(slots).extracting(Slot::startTime, Slot::status).containsExactly(
                tuple(LocalTime.of(8, 0), SlotStatus.AVAILABLE),
                tuple(LocalTime.of(8, 30), SlotStatus.TAKEN),
                tuple(LocalTime.of(9, 0), SlotStatus.AVAILABLE),
                tuple(LocalTime.of(9, 30), SlotStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Una cita agendada con un intervalo más largo ocupa las franjas que cruza")
    void longerAppointmentBlocksSeveralSlots() {
        List<Slot> slots = schedule.slotsOn(MONDAY_DATE, List.of(appointmentAt(LocalTime.of(8, 0), 45)),
                SUNDAY_NIGHT);

        assertThat(slots).extracting(Slot::status).containsExactly(
                SlotStatus.TAKEN, SlotStatus.TAKEN, SlotStatus.AVAILABLE, SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Si la fecha es hoy, las franjas cuya hora ya pasó no se pueden elegir")
    void marksPastSlotsToday() {
        LocalDateTime now = MONDAY_DATE.atTime(8, 30);

        List<Slot> slots = schedule.slotsOn(MONDAY_DATE, List.of(), now);

        assertThat(slots).extracting(Slot::status).containsExactly(
                SlotStatus.PAST, SlotStatus.PAST, SlotStatus.AVAILABLE, SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("No hay franjas en un día que el profesional no atiende")
    void noSlotsOnNonWorkingDay() {
        assertThat(schedule.slotsOn(TUESDAY_DATE, List.of(), SUNDAY_NIGHT)).isEmpty();
    }

    private static Appointment appointmentAt(LocalTime start, int minutes) {
        return Appointment.book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), MONDAY_DATE, start,
                minutes, Instant.parse("2026-09-26T15:00:00Z"));
    }
}
