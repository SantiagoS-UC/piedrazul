package co.edu.unicauca.piedrazul.professionals.infrastructure.persistence;

import co.edu.unicauca.piedrazul.professionals.domain.Professional;
import co.edu.unicauca.piedrazul.professionals.domain.Specialty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "professionals")
class ProfessionalEntity {

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialty specialty;

    @Column(nullable = false)
    private boolean active;

    protected ProfessionalEntity() {
        // Requerido por JPA.
    }

    Professional toDomain() {
        return new Professional(id, firstName, lastName, specialty, active);
    }
}
