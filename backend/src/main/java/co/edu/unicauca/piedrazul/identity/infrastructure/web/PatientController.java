package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.PatientSearchService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasAnyRole('SCHEDULER', 'PROFESSIONAL')")
class PatientController {

    private final PatientSearchService patientSearchService;

    PatientController(PatientSearchService patientSearchService) {
        this.patientSearchService = patientSearchService;
    }

    @GetMapping("/search")
    List<PatientSearchResponse> search(@RequestParam String document) {
        return patientSearchService.searchByDocument(document).stream()
                .map(PatientSearchResponse::from)
                .toList();
    }
}
