package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.identity.PatientSummary;
import java.util.List;

/**
 * @param entries citas del paciente, de la más reciente a la más antigua
 */
public record PatientHistory(PatientSummary patient, List<PatientHistoryEntry> entries) {

    public int total() {
        return entries.size();
    }

    public long upcoming() {
        return entries.stream().filter(PatientHistoryEntry::upcoming).count();
    }

    public long past() {
        return total() - upcoming();
    }
}
