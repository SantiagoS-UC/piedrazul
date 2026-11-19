package co.edu.unicauca.piedrazul.appointments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.AccessForbiddenException;
import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequesterTest {

    private static final UUID OWN_ID = UUID.randomUUID();
    private static final UUID OTHER_ID = UUID.randomUUID();

    @Test
    @DisplayName("El profesional consulta su propia agenda sin indicar su id")
    void professionalDefaultsToOwnAgenda() {
        assertThat(Requester.professional(OWN_ID).agendaOwner(null)).isEqualTo(OWN_ID);
    }

    @Test
    @DisplayName("El profesional puede indicar su propio id")
    void professionalMayPassOwnId() {
        assertThat(Requester.professional(OWN_ID).agendaOwner(OWN_ID)).isEqualTo(OWN_ID);
    }

    @Test
    @DisplayName("El profesional no puede consultar la agenda de otro profesional")
    void professionalCannotSeeOthers() {
        assertThatThrownBy(() -> Requester.professional(OWN_ID).agendaOwner(OTHER_ID))
                .isInstanceOf(AccessForbiddenException.class)
                .hasMessage("Solo puedes consultar tu propia agenda.");
    }

    @Test
    @DisplayName("El agendador consulta la agenda del profesional que elija")
    void schedulerChoosesProfessional() {
        assertThat(Requester.scheduler(OWN_ID).agendaOwner(OTHER_ID)).isEqualTo(OTHER_ID);
    }

    @Test
    @DisplayName("El agendador debe elegir un profesional")
    void schedulerMustChooseProfessional() {
        assertThatThrownBy(() -> Requester.scheduler(OWN_ID).agendaOwner(null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Elige un profesional.");
    }

    @Test
    @DisplayName("Exige el id de la cuenta")
    void requiresUserId() {
        assertThatThrownBy(() -> Requester.scheduler(null)).isInstanceOf(NullPointerException.class);
    }
}
