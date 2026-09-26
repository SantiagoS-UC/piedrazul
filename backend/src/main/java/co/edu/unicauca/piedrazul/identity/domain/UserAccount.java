package co.edu.unicauca.piedrazul.identity.domain;

import co.edu.unicauca.piedrazul.shared.domain.AuthenticationFailedException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Credenciales y rol de cualquier persona que ingresa al sistema. Los datos personales del
 * paciente viven en {@link Patient}, que comparte el mismo identificador.
 */
public class UserAccount {

    private final UUID id;
    private final Email email;
    private final String passwordHash;
    private final Role role;
    private final String displayName;
    private final boolean active;
    private final Instant createdAt;

    private UserAccount(UUID id, Email email, String passwordHash, Role role, String displayName,
            boolean active, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.displayName = Objects.requireNonNull(displayName);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static UserAccount newPatientAccount(UUID id, Email email, String passwordHash, PersonName name,
            Instant createdAt) {
        return new UserAccount(id, email, passwordHash, Role.PATIENT, name.shortName(), true, createdAt);
    }

    /**
     * Reconstruye una cuenta ya guardada, sin aplicar las reglas de creación.
     */
    public static UserAccount restore(UUID id, Email email, String passwordHash, Role role, String displayName,
            boolean active, Instant createdAt) {
        return new UserAccount(id, email, passwordHash, role, displayName, active, createdAt);
    }

    public void ensureCanLogIn() {
        if (!active) {
            throw new AuthenticationFailedException(
                    "Tu cuenta está inactiva. Comunícate con la clínica Piedrazul para activarla.");
        }
    }

    public UUID id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public String displayName() {
        return displayName;
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
