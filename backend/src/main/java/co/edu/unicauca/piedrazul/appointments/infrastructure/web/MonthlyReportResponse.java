package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.MonthlyAppointmentsReport;
import co.edu.unicauca.piedrazul.appointments.application.ProfessionalMonthlyCounts;
import java.util.List;
import java.util.UUID;

/**
 * Las listas de conteos tienen doce valores, de enero a diciembre.
 */
public record MonthlyReportResponse(
        int year,
        List<Integer> availableYears,
        int total,
        List<Integer> monthTotals,
        List<ProfessionalSeriesResponse> professionals) {

    public record ProfessionalSeriesResponse(UUID id, String name, String specialtyName, List<Integer> counts,
            int total) {

        static ProfessionalSeriesResponse from(ProfessionalMonthlyCounts row) {
            return new ProfessionalSeriesResponse(row.professional().id(), row.professional().fullName(),
                    row.professional().specialtyName(), row.counts(), row.total());
        }
    }

    static MonthlyReportResponse from(MonthlyAppointmentsReport report) {
        return new MonthlyReportResponse(report.year(), report.availableYears(), report.total(),
                report.monthTotals(),
                report.professionals().stream().map(ProfessionalSeriesResponse::from).toList());
    }
}
