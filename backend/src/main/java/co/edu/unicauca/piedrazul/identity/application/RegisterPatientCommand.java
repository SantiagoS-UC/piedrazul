package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import co.edu.unicauca.piedrazul.identity.domain.Gender;
import java.time.LocalDate;

public record RegisterPatientCommand(
        String firstName,
        String middleName,
        String firstLastName,
        String secondLastName,
        DocumentType documentType,
        String documentNumber,
        LocalDate birthDate,
        Gender gender,
        String phone,
        String email,
        String password,
        String passwordConfirmation) {
}
