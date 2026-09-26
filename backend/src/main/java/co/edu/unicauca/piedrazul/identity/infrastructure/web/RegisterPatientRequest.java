package co.edu.unicauca.piedrazul.identity.infrastructure.web;

import co.edu.unicauca.piedrazul.identity.application.RegisterPatientCommand;
import co.edu.unicauca.piedrazul.identity.domain.DocumentType;
import co.edu.unicauca.piedrazul.identity.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// Estas validaciones permiten mostrar todos los campos faltantes a la vez; las reglas de formato
// las aplica el dominio.
record RegisterPatientRequest(
        @NotBlank(message = "El primer nombre es obligatorio.") String firstName,
        String middleName,
        @NotBlank(message = "El primer apellido es obligatorio.") String firstLastName,
        String secondLastName,
        @NotNull(message = "Selecciona el tipo de documento.") DocumentType documentType,
        @NotBlank(message = "El número de documento es obligatorio.") String documentNumber,
        @NotNull(message = "La fecha de nacimiento es obligatoria.") LocalDate birthDate,
        @NotNull(message = "Selecciona el género.") Gender gender,
        @NotBlank(message = "El teléfono es obligatorio.") String phone,
        @NotBlank(message = "El correo electrónico es obligatorio.") String email,
        @NotBlank(message = "La contraseña es obligatoria.") String password,
        @NotBlank(message = "Confirma tu contraseña.") String passwordConfirmation) {

    RegisterPatientCommand toCommand() {
        return new RegisterPatientCommand(firstName, middleName, firstLastName, secondLastName, documentType,
                documentNumber, birthDate, gender, phone, email, password, passwordConfirmation);
    }
}
