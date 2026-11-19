package co.edu.unicauca.piedrazul.appointments.domain;

import co.edu.unicauca.piedrazul.shared.domain.DuplicateResourceException;

/**
 * Otro paciente reservó la franja antes de que este confirmara.
 */
public class SlotTakenException extends DuplicateResourceException {

    public SlotTakenException() {
        super("time", "Ese horario acaba de ser tomado por otra persona. Elige otra hora.");
    }
}
