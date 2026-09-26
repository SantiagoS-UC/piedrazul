package co.edu.unicauca.piedrazul.identity.infrastructure.persistence;

import co.edu.unicauca.piedrazul.identity.domain.Email;
import co.edu.unicauca.piedrazul.identity.domain.Role;
import co.edu.unicauca.piedrazul.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserAccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserAccountEntity() {
        // Requerido por JPA.
    }

    static UserAccountEntity fromDomain(UserAccount account) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.id = account.id();
        entity.email = account.email().value();
        entity.passwordHash = account.passwordHash();
        entity.role = account.role();
        entity.displayName = account.displayName();
        entity.active = account.active();
        entity.createdAt = account.createdAt();
        return entity;
    }

    UserAccount toDomain() {
        return UserAccount.restore(id, new Email(email), passwordHash, role, displayName, active, createdAt);
    }
}
