package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.util.List;

/**
 * @param counts doce valores: citas de enero a diciembre
 */
public record ProfessionalMonthlyCounts(ProfessionalSummary professional, List<Integer> counts) {

    public ProfessionalMonthlyCounts {
        counts = List.copyOf(counts);
        if (counts.size() != 12) {
            throw new IllegalArgumentException("Se esperan los conteos de los doce meses.");
        }
    }

    public int total() {
        return counts.stream().mapToInt(Integer::intValue).sum();
    }
}
