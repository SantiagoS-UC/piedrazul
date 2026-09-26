package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Las horas viajan como HH:mm para que el frontend las compare y muestre sin segundos.
final class TimeFormat {

    private static final DateTimeFormatter HOURS_AND_MINUTES = DateTimeFormatter.ofPattern("HH:mm");

    private TimeFormat() {
    }

    static String format(LocalTime time) {
        return time.format(HOURS_AND_MINUTES);
    }
}
