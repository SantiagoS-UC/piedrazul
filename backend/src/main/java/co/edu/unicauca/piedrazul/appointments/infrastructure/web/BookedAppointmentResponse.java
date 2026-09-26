package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.BookedAppointment;
import java.time.LocalDate;
import java.util.UUID;

public record BookedAppointmentResponse(
        UUID id,
        LocalDate date,
        String time,
        int durationMinutes,
        BookableProfessionalResponse professional) {

    static BookedAppointmentResponse from(BookedAppointment booked) {
        return new BookedAppointmentResponse(
                booked.appointment().id(),
                booked.appointment().date(),
                TimeFormat.format(booked.appointment().startTime()),
                booked.appointment().durationMinutes(),
                BookableProfessionalResponse.from(booked.professional()));
    }
}
