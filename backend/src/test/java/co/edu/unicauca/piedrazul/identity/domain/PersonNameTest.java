package co.edu.unicauca.piedrazul.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonNameTest {

    @Test
    @DisplayName("Arma el nombre completo con los cuatro nombres")
    void fullNameWithAllParts() {
        PersonName name = new PersonName("Ana", "María", "Gómez", "Ruiz");

        assertThat(name.fullName()).isEqualTo("Ana María Gómez Ruiz");
        assertThat(name.shortName()).isEqualTo("Ana Gómez");
    }

    @Test
    @DisplayName("Omite el segundo nombre y el segundo apellido cuando no existen")
    void fullNameWithoutOptionalParts() {
        PersonName name = new PersonName("Carlos", " ", "Muñoz", null);

        assertThat(name.middleName()).isNull();
        assertThat(name.secondLastName()).isNull();
        assertThat(name.fullName()).isEqualTo("Carlos Muñoz");
    }

    @Test
    @DisplayName("Quita espacios sobrantes y acepta tildes, eñes y apóstrofes")
    void normalizesSpacesAndAcceptsAccents() {
        PersonName name = new PersonName("  José   Luis ", null, "Núñez", "D'Angelo");

        assertThat(name.firstName()).isEqualTo("José Luis");
        assertThat(name.secondLastName()).isEqualTo("D'Angelo");
    }

    @Test
    @DisplayName("Exige el primer nombre")
    void requiresFirstName() {
        assertThatThrownBy(() -> new PersonName(null, null, "Gómez", null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El primer nombre es obligatorio.")
                .extracting(ex -> ((BusinessRuleViolationException) ex).field().orElseThrow())
                .isEqualTo("firstName");
    }

    @Test
    @DisplayName("Exige el primer apellido")
    void requiresFirstLastName() {
        assertThatThrownBy(() -> new PersonName("Ana", null, "", null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("El primer apellido es obligatorio.");
    }

    @Test
    @DisplayName("Rechaza números o símbolos en el nombre")
    void rejectsNonLetters() {
        assertThatThrownBy(() -> new PersonName("Ana2", null, "Gómez", null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Solo puede contener letras");
    }

    @Test
    @DisplayName("Rechaza nombres de más de 60 caracteres")
    void rejectsTooLong() {
        String tooLong = "A".repeat(61);

        assertThatThrownBy(() -> new PersonName("Ana", null, tooLong, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
