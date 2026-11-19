package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Usuario que hace la consulta, tomado del token.
 *
 * @param professional {@code true} para médicos y terapistas, {@code false} para el agendador
 */
public record Requester(UUID userId, boolean professional) {

    public Requester {
        Objects.requireNonNull(userId);
    }

    public static Requester scheduler(UUID userId) {
        return new Requester(userId, false);
    }

    public static Requester professional(UUID userId) {
        return new Requester(userId, true);
    }

    /**
     * El profesional solo puede ver su propia agenda; el agendador debe indicar de quién.
     *
     * @param requestedProfessionalId puede ser {@code null}
     */
    public UUID agendaOwner(UUID requestedProfessionalId) {
        if (professional) {
            if (requestedProfessionalId != null && !requestedProfessionalId.equals(userId)) {
                throw new AccessForbiddenException("Solo puedes consultar tu propia agenda.");
            }
            return userId;
        }
        if (requestedProfessionalId == null) {
            throw new BusinessRuleViolationException("professionalId", "Elige un profesional.");
        }
        return requestedProfessionalId;
    }
}
