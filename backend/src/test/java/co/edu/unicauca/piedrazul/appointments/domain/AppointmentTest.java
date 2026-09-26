package co.edu.unicauca.piedrazul.appointments.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    @Test
    @DisplayName("Registra la cita con su duración y calcula la hora de fin")
    void booksAppointment() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);

        assertThat(appointment.date()).isEqualTo(DATE);
        assertThat(appointment.startTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(appointment.endTime()).isEqualTo(LocalTime.of(8, 30));
    }

    @Test
    @DisplayName("No acepta una duración de cero minutos")
    void rejectsZeroDuration() {
        assertThatThrownBy(() -> appointmentAt(LocalTime.of(8, 0), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Se cruza con una franja que empieza a la misma hora")
    void overlapsSameStart() {
        assertThat(appointmentAt(LocalTime.of(8, 0), 30).overlaps(DATE, LocalTime.of(8, 0), 30)).isTrue();
    }

    @Test
    @DisplayName("Se cruza con una franja que empieza antes de que la cita termine")
    void overlapsPartially() {
        // La cita se agendó con 45 minutos y hoy el profesional atiende cada 30.
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 45);

        assertThat(appointment.overlaps(DATE, LocalTime.of(8, 30), 30)).isTrue();
        assertThat(appointment.overlaps(DATE, LocalTime.of(7, 30), 45)).isTrue();
    }

    @Test
    @DisplayName("No se cruza con franjas contiguas")
    void doesNotOverlapAdjacentSlots() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 30), 30);

        assertThat(appointment.overlaps(DATE, LocalTime.of(8, 0), 30)).isFalse();
        assertThat(appointment.overlaps(DATE, LocalTime.of(9, 0), 30)).isFalse();
    }

    @Test
    @DisplayName("No se cruza con una franja de otra fecha")
    void doesNotOverlapOtherDate() {
        assertThat(appointmentAt(LocalTime.of(8, 0), 30).overlaps(DATE.plusDays(1), LocalTime.of(8, 0), 30))
                .isFalse();
    }

    private static Appointment appointmentAt(LocalTime start, int minutes) {
        return Appointment.book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), DATE, start, minutes,
                Instant.parse("2026-09-26T15:00:00Z"));
    }
}
