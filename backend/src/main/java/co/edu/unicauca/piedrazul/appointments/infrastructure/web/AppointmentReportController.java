package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.AppointmentReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF7: datos del reporte gráfico de citas por mes y profesional. El frontend dibuja la gráfica.
 */
@RestController
@RequestMapping("/api/appointments/report")
@PreAuthorize("hasAnyRole('PROFESSIONAL', 'SCHEDULER', 'ADMIN')")
public class AppointmentReportController {

    private final AppointmentReportService appointmentReportService;

    public AppointmentReportController(AppointmentReportService appointmentReportService) {
        this.appointmentReportService = appointmentReportService;
    }

    /** Sin año, devuelve el del año actual. */
    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(@RequestParam(required = false) Integer year) {
        return MonthlyReportResponse.from(appointmentReportService.monthlyReport(year));
    }
}
