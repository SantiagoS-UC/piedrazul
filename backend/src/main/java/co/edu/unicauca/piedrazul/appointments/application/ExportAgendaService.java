package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * RF4: exporta las citas de un profesional en una fecha para organizar la jornada de atención.
 */
@Service
public class ExportAgendaService {

    private final AgendaQueryService agendaQueryService;
    private final AgendaExporter agendaExporter;

    ExportAgendaService(AgendaQueryService agendaQueryService, AgendaExporter agendaExporter) {
        this.agendaQueryService = agendaQueryService;
        this.agendaExporter = agendaExporter;
    }

    public ExportedFile export(Requester requester, UUID requestedProfessionalId, LocalDate date) {
        ProfessionalAgenda agenda = agendaQueryService.agendaOf(requester, requestedProfessionalId, date);
        if (agenda.total() == 0) {
            throw new BusinessRuleViolationException("date",
                    "No hay citas para exportar en esta fecha. Elige otra fecha.");
        }
        return agendaExporter.export(agenda);
    }
}
