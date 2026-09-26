package co.edu.unicauca.piedrazul.identity;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Datos de contacto de un paciente. Usa tipos simples para no exponer el modelo interno del módulo.
 *
 * @param middleName     puede ser {@code null}
 * @param secondLastName puede ser {@code null}
 * @param documentType   CC, TI, CE o PA
 */
public record PatientSummary(
        UUID id,
        String firstName,
        String middleName,
        String firstLastName,
        String secondLastName,
        String documentType,
        String documentNumber,
        String phone) {

    public String givenNames() {
        return join(firstName, middleName);
    }

    public String lastNames() {
        return join(firstLastName, secondLastName);
    }

    private static String join(String... parts) {
        return Stream.of(parts).filter(Objects::nonNull).collect(Collectors.joining(" "));
    }
}
