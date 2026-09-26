package co.edu.unicauca.piedrazul.configuration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla de una sola fila: los parámetros globales del sistema.
@Entity
@Table(name = "scheduling_settings")
class SchedulingSettingsEntity {

    static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Column(name = "window_weeks", nullable = false)
    private Integer windowWeeks;

    protected SchedulingSettingsEntity() {
        // Requerido por JPA.
    }

    SchedulingSettingsEntity(int windowWeeks) {
        this.id = SINGLETON_ID;
        this.windowWeeks = windowWeeks;
    }

    int windowWeeks() {
        return windowWeeks;
    }
}
