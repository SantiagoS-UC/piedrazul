package co.edu.unicauca.piedrazul.configuration;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @param slotStartTimes horas de inicio de todas las franjas de un día de atención, en orden
 */
public record AvailabilitySummary(
        UUID professionalId,
        Set<DayOfWeek> workingDays,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes,
        List<LocalTime> slotStartTimes) {

    public boolean worksOn(DayOfWeek day) {
        return workingDays.contains(day);
    }
}
