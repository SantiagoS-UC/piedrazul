package co.edu.unicauca.piedrazul.configuration.domain;

public interface SchedulingWindowRepository {

    /**
     * La ventana vigente, o {@link SchedulingWindow#DEFAULT} si nunca se ha configurado.
     */
    SchedulingWindow current();

    void save(SchedulingWindow window);
}
