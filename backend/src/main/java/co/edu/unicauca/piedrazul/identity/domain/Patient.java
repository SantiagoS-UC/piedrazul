package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Patient {

    private final UUID id;
    private final PersonName name;
    private final IdentityDocument document;
    private final LocalDate birthDate;
    private final Gender gender;
    private final PhoneNumber phone;

    private Patient(UUID id, PersonName name, IdentityDocument document, LocalDate birthDate, Gender gender,
            PhoneNumber phone) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.document = Objects.requireNonNull(document);
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = Objects.requireNonNull(phone);
    }

    /**
     * @param today fecha actual en la zona horaria de la clínica, para validar la fecha de nacimiento
     */
    public static Patient register(UUID id, PersonName name, IdentityDocument document, LocalDate birthDate,
            Gender gender, PhoneNumber phone, LocalDate today) {
        if (birthDate == null) {
            throw new BusinessRuleViolationException("birthDate", "La fecha de nacimiento es obligatoria.");
        }
        if (birthDate.isAfter(today)) {
            throw new BusinessRuleViolationException("birthDate", "La fecha de nacimiento no puede ser futura.");
        }
        if (gender == null) {
            throw new BusinessRuleViolationException("gender", "Selecciona el género.");
        }
        return new Patient(id, name, document, birthDate, gender, phone);
    }

    public static Patient restore(UUID id, PersonName name, IdentityDocument document, LocalDate birthDate,
            Gender gender, PhoneNumber phone) {
        return new Patient(id, name, document, birthDate, gender, phone);
    }

    public UUID id() {
        return id;
    }

    public PersonName name() {
        return name;
    }

    public IdentityDocument document() {
        return document;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public Gender gender() {
        return gender;
    }

    public PhoneNumber phone() {
        return phone;
    }
}
