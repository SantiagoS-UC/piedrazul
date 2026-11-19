package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.PatientSummary;
import java.util.UUID;

record PatientSearchResponse(UUID id, String givenNames, String lastNames, String documentType,
        String documentNumber) {

    static PatientSearchResponse from(PatientSummary patient) {
        return new PatientSearchResponse(patient.id(), patient.givenNames(), patient.lastNames(),
                patient.documentType(), patient.documentNumber());
    }
}
