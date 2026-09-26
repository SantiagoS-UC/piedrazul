package co.edu.unicauca.piedrazul.identity.application;

import co.edu.unicauca.piedrazul.identity.domain.Role;
import java.time.Instant;

public record LoginResult(String token, Instant expiresAt, Role role, String displayName) {
}
