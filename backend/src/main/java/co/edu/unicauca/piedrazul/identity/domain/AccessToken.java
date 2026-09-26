package co.edu.unicauca.piedrazul.identity.domain;

import java.time.Instant;

public record AccessToken(String value, Instant expiresAt) {
}
