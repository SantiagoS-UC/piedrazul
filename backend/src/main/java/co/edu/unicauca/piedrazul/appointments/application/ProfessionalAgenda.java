package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.time.LocalDate;
import java.util.List;

/**
 * @param entries citas del día ordenadas por hora
 */
public record ProfessionalAgenda(ProfessionalSummary professional, LocalDate date, List<AgendaEntry> entries) {

    public int total() {
        return entries.size();
    }
}
