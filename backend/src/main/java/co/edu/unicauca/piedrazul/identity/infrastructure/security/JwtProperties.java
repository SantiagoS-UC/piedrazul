package co.edu.unicauca.piedrazul.identity.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret     clave para firmar los tokens con HMAC-SHA256
 * @param expiration tiempo de vida de cada token
 */
@ConfigurationProperties("piedrazul.security.jwt")
record JwtProperties(String secret, Duration expiration) {

    // HS256 exige una clave de al menos 256 bits.
    private static final int MIN_SECRET_BYTES = 32;

    JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "piedrazul.security.jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " bytes");
        }
        if (expiration == null || expiration.isNegative() || expiration.isZero()) {
            throw new IllegalStateException("piedrazul.security.jwt.expiration debe ser positiva");
        }
    }
}
