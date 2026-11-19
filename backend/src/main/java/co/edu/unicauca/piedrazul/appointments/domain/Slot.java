package co.edu.unicauca.piedrazul.appointments.domain;

import java.time.LocalTime;

public record Slot(LocalTime startTime, SlotStatus status) {

    public boolean available() {
        return status == SlotStatus.AVAILABLE;
    }
}
