package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.AgendaQueryService;
import co.edu.unicauca.piedrazul.appointments.application.ExportAgendaService;
import co.edu.unicauca.piedrazul.appointments.application.ExportedFile;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-04 y RF4: citas de un profesional en una fecha, en pantalla o como archivo CSV. El agendador
 * elige el profesional; el médico o terapista solo puede ver la suya y no necesita enviar su id.
 */
@RestController
@RequestMapping("/api/appointments/agenda")
@PreAuthorize("hasAnyRole('SCHEDULER', 'PROFESSIONAL')")
public class AgendaController {

    private final AgendaQueryService agendaQueryService;
    private final ExportAgendaService exportAgendaService;

    public AgendaController(AgendaQueryService agendaQueryService, ExportAgendaService exportAgendaService) {
        this.agendaQueryService = agendaQueryService;
        this.exportAgendaService = exportAgendaService;
    }

    @GetMapping
    public AgendaResponse agenda(Authentication authentication, @RequestParam(required = false) UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return AgendaResponse.from(
                agendaQueryService.agendaOf(CurrentRequester.from(authentication), professionalId, date));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(Authentication authentication,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ExportedFile file = exportAgendaService.export(CurrentRequester.from(authentication), professionalId, date);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}
