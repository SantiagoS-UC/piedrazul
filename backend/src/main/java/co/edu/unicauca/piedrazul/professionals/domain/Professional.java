package co.edu.unicauca.piedrazul.professionals.domain;

import co.edu.unicauca.piedrazul.shared.domain.AlphabeticalOrder;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class Professional {

    /** Orden alfabético por apellido y luego por nombre. */
    public static final Comparator<Professional> ALPHABETICAL = Comparator
            .comparing(Professional::lastName, AlphabeticalOrder.spanish())
            .thenComparing(Professional::firstName, AlphabeticalOrder.spanish());

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final Specialty specialty;
    private final boolean active;

    public Professional(UUID id, String firstName, String lastName, Specialty specialty, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.firstName = requireText(firstName, "El nombre del profesional es obligatorio.");
        this.lastName = requireText(lastName, "El apellido del profesional es obligatorio.");
        this.specialty = Objects.requireNonNull(specialty);
        this.active = active;
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public ProfessionalType type() {
        return specialty.professionalType();
    }

    public UUID id() {
        return id;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public Specialty specialty() {
        return specialty;
    }

    public boolean active() {
        return active;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(message);
        }
        return value.trim();
    }
}
