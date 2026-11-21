package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.RescheduleAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.application.RescheduleAppointmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cambia la fecha y la hora de una cita. Solo se envían los datos que cambian, por eso es PATCH.
 */
@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('SCHEDULER', 'PROFESSIONAL')")
public class RescheduleController {

    private final RescheduleAppointmentService rescheduleAppointmentService;

    public RescheduleController(RescheduleAppointmentService rescheduleAppointmentService) {
        this.rescheduleAppointmentService = rescheduleAppointmentService;
    }

    @PatchMapping("/{appointmentId}")
    public BookedAppointmentResponse reschedule(Authentication authentication, @PathVariable UUID appointmentId,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        RescheduleAppointmentCommand command = new RescheduleAppointmentCommand(appointmentId, request.date(),
                request.time());
        return BookedAppointmentResponse.from(
                rescheduleAppointmentService.reschedule(CurrentRequester.from(authentication), command));
    }
}
