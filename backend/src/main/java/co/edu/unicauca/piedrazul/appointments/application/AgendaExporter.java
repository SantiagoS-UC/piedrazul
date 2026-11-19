package co.edu.unicauca.piedrazul.appointments.application;

/**
 * Convierte una agenda en un archivo. Cada formato es una implementación distinta (Strategy).
 */
public interface AgendaExporter {

    ExportedFile export(ProfessionalAgenda agenda);
}
