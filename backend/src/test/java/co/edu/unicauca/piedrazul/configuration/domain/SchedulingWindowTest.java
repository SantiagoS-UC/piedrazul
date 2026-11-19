package co.edu.unicauca.piedrazul.configuration.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SchedulingWindowTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 12})
    @DisplayName("Acepta ventanas entre 1 y 12 semanas")
    void acceptsValidRange(int weeks) {
        assertThat(new SchedulingWindow(weeks).weeks()).isEqualTo(weeks);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 13})
    @DisplayName("Rechaza ventanas fuera del rango permitido")
    void rejectsOutOfRange(int weeks) {
        assertThatThrownBy(() -> new SchedulingWindow(weeks))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("La ventana debe estar entre 1 y 12 semanas.");
    }

    @Test
    @DisplayName("La ventana por defecto es de 4 semanas")
    void defaultIsFourWeeks() {
        assertThat(SchedulingWindow.DEFAULT.weeks()).isEqualTo(4);
    }

    @Test
    @DisplayName("Incluye desde hoy hasta el último día de la ventana")
    void includesDatesInsideTheWindow() {
        SchedulingWindow window = new SchedulingWindow(2);

        assertThat(window.lastBookableDate(TODAY)).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(window.includes(TODAY, TODAY)).isTrue();
        assertThat(window.includes(LocalDate.of(2026, 10, 12), TODAY)).isTrue();
    }

    @Test
    @DisplayName("Excluye fechas pasadas y posteriores a la ventana")
    void excludesDatesOutsideTheWindow() {
        SchedulingWindow window = new SchedulingWindow(2);

        assertThat(window.includes(TODAY.minusDays(1), TODAY)).isFalse();
        assertThat(window.includes(LocalDate.of(2026, 10, 13), TODAY)).isFalse();
    }
}
