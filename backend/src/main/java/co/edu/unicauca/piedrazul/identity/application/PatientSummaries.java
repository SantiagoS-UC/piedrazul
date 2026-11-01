package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.identity.domain.Patient;

final class PatientSummaries {

    private PatientSummaries() {
    }

    static PatientSummary of(Patient patient) {
        return new PatientSummary(
                patient.id(),
                patient.name().firstName(),
                patient.name().middleName(),
                patient.name().firstLastName(),
                patient.name().secondLastName(),
                patient.document().type().name(),
                patient.document().number(),
                patient.phone().value());
    }
}
