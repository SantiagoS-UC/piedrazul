package co.edu.unicauca.piedrazul.appointments.application;

import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.Objects;
import java.util.UUID;

/**
 * Quién hace la consulta. El rol ya lo validó {@code @PreAuthorize}; aquí viven las reglas que
 * dependen de la persona, como que un profesional solo ve su propia agenda.
 *
 * @param userId       identificador de la cuenta, tomado del token
 * @param professional si es médico o terapista; si no, es el agendador
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
     * Decide de qué profesional es la agenda que se consulta. El profesional no necesita indicar
     * su id porque es el de su cuenta; el agendador sí debe elegir uno.
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
