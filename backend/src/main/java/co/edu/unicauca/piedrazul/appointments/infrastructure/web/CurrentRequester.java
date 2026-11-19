package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.Requester;
import java.util.UUID;
import org.springframework.security.core.Authentication;

// La capa de aplicación recibe un Requester y no depende de Spring Security.
final class CurrentRequester {

    private static final String PROFESSIONAL_AUTHORITY = "ROLE_PROFESSIONAL";

    private CurrentRequester() {
    }

    static Requester from(Authentication authentication) {
        // Con JWT, getName() devuelve el claim sub, que es el id de la cuenta.
        UUID userId = UUID.fromString(authentication.getName());
        boolean professional = authentication.getAuthorities().stream()
                .anyMatch(authority -> PROFESSIONAL_AUTHORITY.equals(authority.getAuthority()));
        return new Requester(userId, professional);
    }
}
