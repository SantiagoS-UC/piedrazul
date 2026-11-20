package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.StaffBookedAppointment;
import java.time.LocalDate;
import java.util.UUID;

/**
 * @param patientName     nombres y apellidos del paciente
 * @param patientDocument tipo y número, por ejemplo "CC 1061000001"
 */
public record StaffBookedAppointmentResponse(
        UUID id,
        LocalDate date,
        String time,
        int durationMinutes,
        BookableProfessionalResponse professional,
        String patientName,
        String patientDocument) {

    static StaffBookedAppointmentResponse from(StaffBookedAppointment staffBooked) {
        BookedAppointmentResponse booked = BookedAppointmentResponse.from(staffBooked.booked());
        return new StaffBookedAppointmentResponse(booked.id(), booked.date(), booked.time(), booked.durationMinutes(),
                booked.professional(),
                staffBooked.patient().givenNames() + " " + staffBooked.patient().lastNames(),
                staffBooked.patient().documentType() + " " + staffBooked.patient().documentNumber());
    }
}
