package co.edu.unicauca.piedrazul.shared.domain;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Orden alfabético del español: ignora mayúsculas y tildes, de modo que "Álvarez" queda junto a
 * "Alvarado" y no después de la Z, como ocurriría comparando los textos directamente.
 */
public final class AlphabeticalOrder {

    private static final Collator SPANISH = createCollator();

    private AlphabeticalOrder() {
    }

    public static Comparator<String> spanish() {
        return SPANISH::compare;
    }

    private static Collator createCollator() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es-CO"));
        collator.setStrength(Collator.PRIMARY);
        return collator;
    }
}
