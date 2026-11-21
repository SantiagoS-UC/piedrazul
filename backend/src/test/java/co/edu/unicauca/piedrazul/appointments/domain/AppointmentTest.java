package co.edu.unicauca.piedrazul.appointments.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 28);

    @Test
    @DisplayName("Al reagendarla conserva su id, su paciente y su fecha de creación")
    void reschedulesKeepingIdentity() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);

        Appointment moved = appointment.rescheduleTo(DATE.plusDays(2), LocalTime.of(10, 0), 20);

        assertThat(moved.id()).isEqualTo(appointment.id());
        assertThat(moved.patientId()).isEqualTo(appointment.patientId());
        assertThat(moved.createdAt()).isEqualTo(appointment.createdAt());
        assertThat(moved.startsAt(DATE.plusDays(2), LocalTime.of(10, 0))).isTrue();
        assertThat(moved.endTime()).isEqualTo(LocalTime.of(10, 20));
        assertThat(appointment.startsAt(DATE, LocalTime.of(8, 0))).isTrue();
    }

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

    @Test
    @DisplayName("Es próxima mientras no haya llegado su hora de inicio")
    void isUpcomingBeforeStart() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);

        assertThat(appointment.isUpcomingAt(DATE.atTime(7, 59))).isTrue();
        assertThat(appointment.isUpcomingAt(DATE.minusDays(1).atTime(18, 0))).isTrue();
    }

    @Test
    @DisplayName("Deja de ser próxima desde su hora de inicio")
    void isPastFromStart() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);

        assertThat(appointment.isUpcomingAt(DATE.atTime(8, 0))).isFalse();
        assertThat(appointment.isUpcomingAt(DATE.plusDays(1).atTime(7, 0))).isFalse();
    }

    @Test
    @DisplayName("Se cancela con el motivo sin espacios sobrantes y deja de estar vigente")
    void cancelsWithReason() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);
        Instant now = Instant.parse("2026-09-26T13:00:00Z");

        Appointment cancelled = appointment.cancel("  Tengo un viaje de trabajo  ", now, DATE.minusDays(2));

        assertThat(cancelled.isCancelled()).isTrue();
        assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(cancelled.cancellation()).contains(new Cancellation("Tengo un viaje de trabajo", now));
        assertThat(cancelled.id()).isEqualTo(appointment.id());
        assertThat(appointment.isCancelled()).isFalse();
    }

    @Test
    @DisplayName("Se puede cancelar hasta dos días antes, no después")
    void requiresTwoDaysInAdvance() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);

        assertThat(appointment.canBeCancelledOn(DATE.minusDays(3))).isTrue();
        assertThat(appointment.canBeCancelledOn(DATE.minusDays(2))).isTrue();
        assertThat(appointment.canBeCancelledOn(DATE.minusDays(1))).isFalse();
        assertThat(appointment.canBeCancelledOn(DATE)).isFalse();
        assertThatThrownBy(() -> appointment.cancel("Tengo un viaje de trabajo", Instant.now(), DATE.minusDays(1)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("mínimo 2 días de anticipación");
    }

    @Test
    @DisplayName("Exige un motivo de al menos 10 caracteres")
    void requiresReason() {
        Appointment appointment = appointmentAt(LocalTime.of(8, 0), 30);
        LocalDate today = DATE.minusDays(5);

        assertThatThrownBy(() -> appointment.cancel("   ", Instant.now(), today))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Escribe el motivo de la cancelación.");
        assertThatThrownBy(() -> appointment.cancel("No puedo", Instant.now(), today))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("al menos 10 caracteres");
    }

    @Test
    @DisplayName("Una cita cancelada no se vuelve a cancelar ni se puede reagendar")
    void cancelledIsFinal() {
        Appointment cancelled = appointmentAt(LocalTime.of(8, 0), 30)
                .cancel("Tengo un viaje de trabajo", Instant.now(), DATE.minusDays(5));

        assertThat(cancelled.canBeCancelledOn(DATE.minusDays(5))).isFalse();
        assertThatThrownBy(() -> cancelled.cancel("Otra vez el mismo motivo", Instant.now(), DATE.minusDays(5)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Esta cita ya estaba cancelada.");
        assertThatThrownBy(() -> cancelled.rescheduleTo(DATE.plusDays(1), LocalTime.of(9, 0), 30))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private static Appointment appointmentAt(LocalTime start, int minutes) {
        return Appointment.book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), DATE, start, minutes,
                Instant.parse("2026-09-26T15:00:00Z"));
    }
}
