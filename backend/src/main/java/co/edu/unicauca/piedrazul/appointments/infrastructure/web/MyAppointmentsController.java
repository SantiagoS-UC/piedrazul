package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.CancelAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.application.CancelAppointmentService;
import co.edu.unicauca.piedrazul.appointments.application.PatientHistoryQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Citas del paciente que inició sesión y RF6, cancelación. El paciente sale del token: nadie puede
 * ver ni cancelar citas de otra persona.
 */
@RestController
@RequestMapping("/api/appointments/mine")
@PreAuthorize("hasRole('PATIENT')")
public class MyAppointmentsController {

    private final PatientHistoryQueryService patientHistoryQueryService;
    private final CancelAppointmentService cancelAppointmentService;

    public MyAppointmentsController(PatientHistoryQueryService patientHistoryQueryService,
            CancelAppointmentService cancelAppointmentService) {
        this.patientHistoryQueryService = patientHistoryQueryService;
        this.cancelAppointmentService = cancelAppointmentService;
    }

    /** De la más reciente a la más antigua, incluidas las canceladas. */
    @GetMapping
    public List<MyAppointmentResponse> myAppointments(@AuthenticationPrincipal Jwt jwt) {
        return patientHistoryQueryService.historyOf(UUID.fromString(jwt.getSubject())).entries().stream()
                .map(MyAppointmentResponse::from)
                .toList();
    }

    // POST y no DELETE: la cita no se borra, cambia de estado y guarda el motivo.
    @PostMapping("/{appointmentId}/cancel")
    public MyAppointmentResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request) {
        CancelAppointmentCommand command = new CancelAppointmentCommand(UUID.fromString(jwt.getSubject()),
                appointmentId, request.reason());
        return MyAppointmentResponse.from(cancelAppointmentService.cancel(command));
    }
}
