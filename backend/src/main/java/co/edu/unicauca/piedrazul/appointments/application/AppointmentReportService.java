package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF7: reporte de cantidad de citas por mes y profesional para un año. Cuenta las citas vigentes,
 * ya atendidas o por atender; las canceladas no se cuentan porque no ocuparon la agenda.
 */
@Service
@Transactional(readOnly = true)
public class AppointmentReportService {

    private static final int FIRST_VALID_YEAR = 2000;
    private static final int LAST_VALID_YEAR = 2100;

    private final ProfessionalCatalog professionalCatalog;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    AppointmentReportService(ProfessionalCatalog professionalCatalog, AppointmentRepository appointmentRepository,
            Clock clock) {
        this.professionalCatalog = professionalCatalog;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    /**
     * @param requestedYear {@code null} para el año actual
     */
    public MonthlyAppointmentsReport monthlyReport(Integer requestedYear) {
        int currentYear = Year.now(clock).getValue();
        int year = requestedYear == null ? currentYear : requestedYear;
        if (year < FIRST_VALID_YEAR || year > LAST_VALID_YEAR) {
            throw new BusinessRuleViolationException("year", "Elige un año válido.");
        }

        // Pocas citas por año en una clínica: se cuentan en memoria en lugar de agrupar en SQL.
        Map<UUID, int[]> countsByProfessional = new HashMap<>();
        for (Appointment appointment : appointmentRepository.findScheduledBetween(LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31))) {
            int[] counts = countsByProfessional.computeIfAbsent(appointment.professionalId(), id -> new int[12]);
            counts[appointment.date().getMonthValue() - 1]++;
        }

        // Los inactivos solo aparecen si tuvieron citas ese año.
        List<ProfessionalMonthlyCounts> rows = professionalCatalog.findAll().stream()
                .filter(professional -> professional.active() || countsByProfessional.containsKey(professional.id()))
                .map(professional -> new ProfessionalMonthlyCounts(professional,
                        toList(countsByProfessional.getOrDefault(professional.id(), new int[12]))))
                .toList();
        return new MonthlyAppointmentsReport(year, availableYears(currentYear), rows);
    }

    /**
     * Desde el año de la primera cita hasta el año actual, o hasta el de la última cita si hay citas
     * agendadas para el año siguiente.
     */
    private List<Integer> availableYears(int currentYear) {
        int first = appointmentRepository.firstScheduledDate()
                .map(LocalDate::getYear)
                .map(year -> Math.min(year, currentYear))
                .orElse(currentYear);
        int last = appointmentRepository.lastScheduledDate()
                .map(LocalDate::getYear)
                .map(year -> Math.max(year, currentYear))
                .orElse(currentYear);
        return IntStream.rangeClosed(first, last).boxed().toList();
    }

    private static List<Integer> toList(int[] counts) {
        return Arrays.stream(counts).boxed().toList();
    }
}
