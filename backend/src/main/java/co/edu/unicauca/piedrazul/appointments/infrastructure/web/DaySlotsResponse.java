package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.domain.Slot;
import java.time.LocalDate;
import java.util.List;

public record DaySlotsResponse(LocalDate date, List<SlotResponse> slots) {

    /**
     * @param time      hora de inicio en formato HH:mm
     * @param available si el paciente puede elegirla
     */
    public record SlotResponse(String time, boolean available) {
    }

    static DaySlotsResponse from(LocalDate date, List<Slot> slots) {
        return new DaySlotsResponse(date, slots.stream()
                .map(slot -> new SlotResponse(TimeFormat.format(slot.startTime()), slot.available()))
                .toList());
    }
}
