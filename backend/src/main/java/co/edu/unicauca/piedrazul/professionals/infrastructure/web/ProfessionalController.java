package co.edu.unicauca.piedrazul.professionals.infrastructure.web;

import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.domain.Specialty;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Lectura permitida a cualquier usuario autenticado: el paciente elige profesional al agendar,
// el agendador filtra el listado y el administrador configura su disponibilidad.
@RestController
@RequestMapping("/api/professionals")
class ProfessionalController {

    private final ProfessionalCatalog professionalCatalog;

    ProfessionalController(ProfessionalCatalog professionalCatalog) {
        this.professionalCatalog = professionalCatalog;
    }

    @GetMapping
    List<ProfessionalResponse> findAll() {
        return professionalCatalog.findAll().stream().map(ProfessionalResponse::from).toList();
    }

    @GetMapping("/specialties")
    List<SpecialtyResponse> specialties() {
        return Arrays.stream(Specialty.values())
                .map(specialty -> new SpecialtyResponse(specialty.name(), specialty.displayName()))
                .toList();
    }
}
