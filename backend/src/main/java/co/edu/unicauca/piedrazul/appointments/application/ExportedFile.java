package co.edu.unicauca.piedrazul.appointments.application;

public record ExportedFile(String fileName, String contentType, byte[] content) {
}
