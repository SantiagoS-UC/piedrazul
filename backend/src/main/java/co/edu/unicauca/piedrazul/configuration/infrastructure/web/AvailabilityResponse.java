package co.edu.unicauca.piedrazul.configuration.infrastructure.web;

import co.edu.unicauca.piedrazul.configuration.domain.ProfessionalAvailability;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record AvailabilityResponse(
        UUID professionalId,
        Set<DayOfWeek> workingDays,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes,
        List<LocalTime> slotStartTimes) {

    static AvailabilityResponse from(ProfessionalAvailability availability) {
        return new AvailabilityResponse(availability.professionalId(), availability.workingDays(),
                availability.startTime(), availability.endTime(), availability.slotMinutes(),
                availability.slotStartTimes());
    }
}
