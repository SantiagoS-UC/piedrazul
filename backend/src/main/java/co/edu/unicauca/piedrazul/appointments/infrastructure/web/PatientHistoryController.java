package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.PatientHistoryQueryService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF5: el paciente se busca por cédula en /api/patients/search y aquí llega su id.
 */
@RestController
@RequestMapping("/api/appointments/patient-history")
@PreAuthorize("hasAnyRole('SCHEDULER', 'PROFESSIONAL')")
public class PatientHistoryController {

    private final PatientHistoryQueryService patientHistoryQueryService;

    public PatientHistoryController(PatientHistoryQueryService patientHistoryQueryService) {
        this.patientHistoryQueryService = patientHistoryQueryService;
    }

    @GetMapping
    public PatientHistoryResponse history(@RequestParam UUID patientId) {
        return PatientHistoryResponse.from(patientHistoryQueryService.historyOf(patientId));
    }
}
