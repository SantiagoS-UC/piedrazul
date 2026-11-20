package co.edu.unicauca.piedrazul.appointments.application;

import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.ANDRES;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.CLOCK;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.LAURA;
import static co.edu.unicauca.piedrazul.appointments.application.BookingFixtures.professional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import co.edu.unicauca.piedrazul.appointments.domain.Appointment;
import co.edu.unicauca.piedrazul.appointments.domain.AppointmentRepository;
import co.edu.unicauca.piedrazul.professionals.ProfessionalCatalog;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentReportServiceTest {

    // El reloj de BookingFixtures está en 2026.
    private static final LocalDate JANUARY_FIRST = LocalDate.of(2026, 1, 1);
    private static final LocalDate DECEMBER_LAST = LocalDate.of(2026, 12, 31);

    @Mock
    private ProfessionalCatalog professionalCatalog;
    @Mock
    private AppointmentRepository appointmentRepository;

    private AppointmentReportService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentReportService(professionalCatalog, appointmentRepository, CLOCK);
        lenient().when(professionalCatalog.findAll()).thenReturn(List.of(LAURA, ANDRES));
        lenient().when(appointmentRepository.firstScheduledDate()).thenReturn(Optional.of(LocalDate.of(2025, 11, 3)));
        lenient().when(appointmentRepository.lastScheduledDate()).thenReturn(Optional.of(LocalDate.of(2026, 10, 20)));
    }

    @Test
    @DisplayName("Cuenta las citas de cada profesional por mes del año pedido")
    void countsByMonthAndProfessional() {
        when(appointmentRepository.findScheduledBetween(JANUARY_FIRST, DECEMBER_LAST)).thenReturn(List.of(
                appointmentOf(LAURA, LocalDate.of(2026, 1, 12)),
                appointmentOf(LAURA, LocalDate.of(2026, 1, 14)),
                appointmentOf(LAURA, LocalDate.of(2026, 9, 30)),
                appointmentOf(ANDRES, LocalDate.of(2026, 12, 2))));

        MonthlyAppointmentsReport report = service.monthlyReport(2026);

        assertThat(report.year()).isEqualTo(2026);
        assertThat(report.professionals()).extracting(ProfessionalMonthlyCounts::professional)
                .containsExactly(LAURA, ANDRES);
        assertThat(report.professionals().get(0).counts()).isEqualTo(List.of(2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0));
        assertThat(report.professionals().get(1).counts()).isEqualTo(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1));
        assertThat(report.monthTotals()).isEqualTo(List.of(2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1));
        assertThat(report.total()).isEqualTo(4);
    }

    @Test
    @DisplayName("Sin año usa el actual y ofrece los años desde la primera cita")
    void defaultsToCurrentYear() {
        when(appointmentRepository.findScheduledBetween(JANUARY_FIRST, DECEMBER_LAST)).thenReturn(List.of());

        MonthlyAppointmentsReport report = service.monthlyReport(null);

        assertThat(report.year()).isEqualTo(2026);
        assertThat(report.availableYears()).isEqualTo(List.of(2025, 2026));
        assertThat(report.total()).isZero();
    }

    @Test
    @DisplayName("Un profesional inactivo solo aparece si tuvo citas ese año")
    void showsInactiveProfessionalsOnlyWithAppointments() {
        ProfessionalSummary retired = professional("Jorge", "Castillo", "CHIROPRACTIC", "Quiropraxia", false);
        ProfessionalSummary alsoRetired = professional("Paula", "Zambrano", "PHYSIOTHERAPY", "Fisioterapia", false);
        when(professionalCatalog.findAll()).thenReturn(List.of(retired, LAURA, alsoRetired));
        when(appointmentRepository.findScheduledBetween(JANUARY_FIRST, DECEMBER_LAST))
                .thenReturn(List.of(appointmentOf(retired, LocalDate.of(2026, 3, 4))));

        MonthlyAppointmentsReport report = service.monthlyReport(2026);

        assertThat(report.professionals()).extracting(ProfessionalMonthlyCounts::professional)
                .containsExactly(retired, LAURA);
    }

    @Test
    @DisplayName("Rechaza un año fuera de rango")
    void rejectsInvalidYear() {
        assertThatThrownBy(() -> service.monthlyReport(1999))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Elige un año válido.");
    }

    private static Appointment appointmentOf(ProfessionalSummary professional, LocalDate date) {
        return Appointment.book(UUID.randomUUID(), UUID.randomUUID(), professional.id(), date, LocalTime.of(8, 0), 30,
                Instant.parse("2025-12-01T15:00:00Z"));
    }
}
