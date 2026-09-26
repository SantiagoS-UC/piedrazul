package co.edu.unicauca.piedrazul.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlphabeticalOrderTest {

    @Test
    @DisplayName("Ordena como en español: las tildes y mayúsculas no alteran el orden")
    void sortsLikeSpanish() {
        List<String> sorted = List.of("zambrano", "Álvarez", "López", "alvarado", "Ñañez", "Nuñez").stream()
                .sorted(AlphabeticalOrder.spanish())
                .toList();

        assertThat(sorted).containsExactly("alvarado", "Álvarez", "López", "Nuñez", "Ñañez", "zambrano");
    }
}
