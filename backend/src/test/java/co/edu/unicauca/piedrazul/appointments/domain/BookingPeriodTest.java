package co.edu.unicauca.piedrazul.appointments.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookingPeriodTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);
    private final BookingPeriod period = new BookingPeriod(TODAY, TODAY.plusWeeks(4));

    @Test
    @DisplayName("Incluye el primer y el último día del periodo")
    void includesBothEnds() {
        assertThat(period.includes(TODAY)).isTrue();
        assertThat(period.includes(TODAY.plusWeeks(4))).isTrue();
    }

    @Test
    @DisplayName("Excluye fechas pasadas y posteriores a la ventana")
    void excludesOutsideDates() {
        assertThat(period.includes(TODAY.minusDays(1))).isFalse();
        assertThat(period.includes(TODAY.plusWeeks(4).plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("No acepta un periodo que termina antes de empezar")
    void rejectsInvertedPeriod() {
        assertThatThrownBy(() -> new BookingPeriod(TODAY, TODAY.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
