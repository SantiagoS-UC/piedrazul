package co.edu.unicauca.piedrazul.configuration.infrastructure.web;

import co.edu.unicauca.piedrazul.configuration.application.AvailabilityService;
import co.edu.unicauca.piedrazul.configuration.application.SaveAvailabilityCommand;
import co.edu.unicauca.piedrazul.configuration.application.SchedulingWindowService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationController {

    private final SchedulingWindowService schedulingWindowService;
    private final AvailabilityService availabilityService;

    public ConfigurationController(SchedulingWindowService schedulingWindowService,
            AvailabilityService availabilityService) {
        this.schedulingWindowService = schedulingWindowService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/scheduling-window")
    public SchedulingWindowResponse schedulingWindow() {
        return SchedulingWindowResponse.from(schedulingWindowService.current());
    }

    @PutMapping("/scheduling-window")
    public SchedulingWindowResponse updateSchedulingWindow(@Valid @RequestBody SchedulingWindowRequest request) {
        return SchedulingWindowResponse.from(schedulingWindowService.update(request.weeks()));
    }

    /**
     * Responde 204 si el profesional aún no tiene disponibilidad, para que el formulario empiece vacío.
     */
    @GetMapping("/availabilities/{professionalId}")
    public ResponseEntity<AvailabilityResponse> availability(@PathVariable UUID professionalId) {
        return availabilityService.findByProfessional(professionalId)
                .map(AvailabilityResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/availabilities/{professionalId}")
    public AvailabilityResponse saveAvailability(@PathVariable UUID professionalId,
            @Valid @RequestBody AvailabilityRequest request) {
        SaveAvailabilityCommand command = new SaveAvailabilityCommand(professionalId, request.workingDays(),
                request.startTime(), request.endTime(), request.slotMinutes());
        return AvailabilityResponse.from(availabilityService.save(command));
    }
}
