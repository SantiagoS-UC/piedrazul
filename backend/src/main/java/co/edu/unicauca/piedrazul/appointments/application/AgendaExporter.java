package co.edu.unicauca.piedrazul.appointments.application;

/**
 * Puerto de salida que convierte una agenda en un archivo. Cada formato es una estrategia
 * distinta (CSV hoy; Excel o PDF mañana) y el caso de uso no cambia al agregar una nueva.
 */
public interface AgendaExporter {

    ExportedFile export(ProfessionalAgenda agenda);
}
