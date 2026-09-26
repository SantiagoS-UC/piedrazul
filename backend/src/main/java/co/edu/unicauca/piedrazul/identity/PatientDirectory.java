package co.edu.unicauca.piedrazul.identity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Consulta de datos de pacientes para otros módulos, por ejemplo para mostrar el nombre y el
 * teléfono en el listado de citas. Es la única forma en que otro módulo accede a esta información.
 */
public interface PatientDirectory {

    /**
     * Los identificadores que no correspondan a un paciente se ignoran.
     */
    List<PatientSummary> findByIds(Collection<UUID> patientIds);
}
