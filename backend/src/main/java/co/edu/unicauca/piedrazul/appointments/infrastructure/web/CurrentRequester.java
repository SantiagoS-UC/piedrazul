package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.Requester;
import java.util.UUID;
import org.springframework.security.core.Authentication;

// Traduce el usuario autenticado al modelo del caso de uso, para que la capa de aplicación no
// dependa de Spring Security.
final class CurrentRequester {

    private static final String PROFESSIONAL_AUTHORITY = "ROLE_PROFESSIONAL";

    private CurrentRequester() {
    }

    static Requester from(Authentication authentication) {
        // En un token JWT el nombre de la autenticación es el claim "sub": el id de la cuenta.
        UUID userId = UUID.fromString(authentication.getName());
        boolean professional = authentication.getAuthorities().stream()
                .anyMatch(authority -> PROFESSIONAL_AUTHORITY.equals(authority.getAuthority()));
        return new Requester(userId, professional);
    }
}
