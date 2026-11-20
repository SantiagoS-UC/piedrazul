package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.BookAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.application.BookForPatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * El agendador crea la cita a nombre de un paciente registrado. Para elegir profesional, fecha y
 * hora usa las mismas consultas de BookingController.
 */
@RestController
@RequestMapping("/api/appointments/for-patient")
@PreAuthorize("hasRole('SCHEDULER')")
public class StaffBookingController {

    private final BookForPatientService bookForPatientService;

    public StaffBookingController(BookForPatientService bookForPatientService) {
        this.bookForPatientService = bookForPatientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffBookedAppointmentResponse book(@Valid @RequestBody StaffBookAppointmentRequest request) {
        BookAppointmentCommand command = new BookAppointmentCommand(request.patientId(), request.professionalId(),
                request.date(), request.time());
        return StaffBookedAppointmentResponse.from(bookForPatientService.book(command));
    }
}
