package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.AgendaQueryService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-04: listado de citas para el agendador.
 */
@RestController
@RequestMapping("/api/appointments/agenda")
@PreAuthorize("hasRole('SCHEDULER')")
public class AgendaController {

    private final AgendaQueryService agendaQueryService;

    public AgendaController(AgendaQueryService agendaQueryService) {
        this.agendaQueryService = agendaQueryService;
    }

    @GetMapping
    public AgendaResponse agenda(@RequestParam UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return AgendaResponse.from(agendaQueryService.agendaOf(professionalId, date));
    }
}
