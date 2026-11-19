package co.edu.unicauca.piedrazul.configuration.application;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record SaveAvailabilityCommand(
        UUID professionalId,
        Set<DayOfWeek> workingDays,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes) {
}
