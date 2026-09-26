package co.edu.unicauca.piedrazul.professionals.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unicauca.piedrazul.shared.domain.BusinessRuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfessionalTest {

    @Test
    @DisplayName("Medicina general la atiende un médico y las demás especialidades un terapista")
    void typeDependsOnSpecialty() {
        assertThat(professional("Laura", "Martínez", Specialty.GENERAL_MEDICINE).type())
                .isEqualTo(ProfessionalType.DOCTOR);
        assertThat(professional("Diana", "Rojas", Specialty.NEURAL_THERAPY).type())
                .isEqualTo(ProfessionalType.THERAPIST);
        assertThat(professional("Natalia", "Guzmán", Specialty.CHIROPRACTIC).type())
                .isEqualTo(ProfessionalType.THERAPIST);
        assertThat(professional("Paula", "Zambrano", Specialty.PHYSIOTHERAPY).type())
                .isEqualTo(ProfessionalType.THERAPIST);
    }

    @Test
    @DisplayName("Arma el nombre completo sin espacios sobrantes")
    void fullName() {
        assertThat(professional(" Laura ", "Martínez", Specialty.GENERAL_MEDICINE).fullName())
                .isEqualTo("Laura Martínez");
    }

    @Test
    @DisplayName("Exige nombre y apellido")
    void requiresNames() {
        assertThatThrownBy(() -> professional("", "Martínez", Specialty.GENERAL_MEDICINE))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> professional("Laura", null, Specialty.GENERAL_MEDICINE))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("El orden alfabético es por apellido y luego por nombre")
    void alphabeticalOrder() {
        Professional lopezMauricio = professional("Mauricio", "López", Specialty.PHYSIOTHERAPY);
        Professional benavides = professional("Camilo", "Benavides", Specialty.CHIROPRACTIC);
        Professional lopezAna = professional("Ana", "López", Specialty.PHYSIOTHERAPY);

        List<Professional> sorted = List.of(lopezMauricio, benavides, lopezAna).stream()
                .sorted(Professional.ALPHABETICAL)
                .toList();

        assertThat(sorted).containsExactly(benavides, lopezAna, lopezMauricio);
    }

    @Test
    @DisplayName("Cada especialidad tiene un nombre en español para mostrar")
    void specialtiesHaveDisplayNames() {
        assertThat(Specialty.GENERAL_MEDICINE.displayName()).isEqualTo("Medicina general");
        assertThat(Specialty.NEURAL_THERAPY.displayName()).isEqualTo("Terapia neural");
        assertThat(Specialty.CHIROPRACTIC.displayName()).isEqualTo("Quiropraxia");
        assertThat(Specialty.PHYSIOTHERAPY.displayName()).isEqualTo("Fisioterapia");
    }

    private static Professional professional(String firstName, String lastName, Specialty specialty) {
        return new Professional(UUID.randomUUID(), firstName, lastName, specialty, true);
    }
}
