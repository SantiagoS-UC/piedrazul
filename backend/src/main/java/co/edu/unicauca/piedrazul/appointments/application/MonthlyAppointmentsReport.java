package co.edu.unicauca.piedrazul.appointments.application;

import java.util.List;
import java.util.stream.IntStream;

/**
 * RF7: cantidad de citas vigentes por mes y profesional en un año.
 *
 * @param availableYears años que se pueden consultar, de menor a mayor
 * @param professionals  en orden alfabético por apellido
 */
public record MonthlyAppointmentsReport(int year, List<Integer> availableYears,
        List<ProfessionalMonthlyCounts> professionals) {

    public MonthlyAppointmentsReport {
        availableYears = List.copyOf(availableYears);
        professionals = List.copyOf(professionals);
    }

    /** Doce valores: total de la clínica de enero a diciembre. */
    public List<Integer> monthTotals() {
        return IntStream.range(0, 12)
                .mapToObj(month -> professionals.stream().mapToInt(row -> row.counts().get(month)).sum())
                .toList();
    }

    public int total() {
        return professionals.stream().mapToInt(ProfessionalMonthlyCounts::total).sum();
    }
}
