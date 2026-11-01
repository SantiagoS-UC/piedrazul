package co.edu.unicauca.piedrazul.appointments.application;

/**
 * Archivo listo para descargar.
 *
 * @param contentType tipo MIME, por ejemplo {@code text/csv}
 */
public record ExportedFile(String fileName, String contentType, byte[] content) {
}
