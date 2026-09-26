package co.edu.unicauca.piedrazul.configuration.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;

/**
 * Periodo, en semanas, en el que los pacientes pueden agendar citas por su cuenta.
 */
public record SchedulingWindow(int weeks) {

    public static final int MIN_WEEKS = 1;
    public static final int MAX_WEEKS = 12;
    public static final SchedulingWindow DEFAULT = new SchedulingWindow(4);

    public SchedulingWindow {
        if (weeks < MIN_WEEKS || weeks > MAX_WEEKS) {
            throw new BusinessRuleViolationException("weeks",
                    "La ventana debe estar entre " + MIN_WEEKS + " y " + MAX_WEEKS + " semanas.");
        }
    }

    /**
     * Último día en que se puede agendar, contando desde hoy (incluido).
     */
    public LocalDate lastBookableDate(LocalDate today) {
        return today.plusWeeks(weeks);
    }

    public boolean includes(LocalDate date, LocalDate today) {
        return !date.isBefore(today) && !date.isAfter(lastBookableDate(today));
    }
}
