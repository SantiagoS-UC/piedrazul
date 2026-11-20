package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.PatientHistory;
import co.edu.unicauca.piedrazul.appointments.application.PatientHistoryEntry;
import co.edu.unicauca.piedrazul.appointments.domain.Cancellation;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientHistoryResponse(
        HistoryPatientResponse patient,
        int total,
        long upcoming,
        long past,
        long cancelled,
        List<HistoryAppointmentResponse> appointments) {

    public record HistoryPatientResponse(UUID id, String givenNames, String lastNames, String documentType,
            String documentNumber, String phone) {

        static HistoryPatientResponse from(PatientSummary patient) {
            return new HistoryPatientResponse(patient.id(), patient.givenNames(), patient.lastNames(),
                    patient.documentType(), patient.documentNumber(), patient.phone());
        }
    }

    /**
     * @param time               hora de inicio en formato HH:mm
     * @param status             UPCOMING, PAST o CANCELLED
     * @param cancellationReason motivo que dejó el paciente; {@code null} si la cita no se canceló
     */
    public record HistoryAppointmentResponse(UUID id, LocalDate date, String time, int durationMinutes,
            String professionalName, String specialtyName, String status, String cancellationReason) {
    }

    static PatientHistoryResponse from(PatientHistory history) {
        List<HistoryAppointmentResponse> appointments = history.entries().stream()
                .map(PatientHistoryResponse::toResponse)
                .toList();
        return new PatientHistoryResponse(HistoryPatientResponse.from(history.patient()), history.total(),
                history.upcoming(), history.past(), history.cancelled(), appointments);
    }

    private static HistoryAppointmentResponse toResponse(PatientHistoryEntry entry) {
        String professionalName = entry.professional().map(ProfessionalSummary::fullName).orElse("Profesional no encontrado");
        String specialtyName = entry.professional().map(ProfessionalSummary::specialtyName).orElse("");
        return new HistoryAppointmentResponse(entry.appointment().id(), entry.appointment().date(),
                TimeFormat.format(entry.appointment().startTime()), entry.appointment().durationMinutes(),
                professionalName, specialtyName, statusOf(entry), cancellationReasonOf(entry));
    }

    static String statusOf(PatientHistoryEntry entry) {
        if (entry.appointment().isCancelled()) {
            return "CANCELLED";
        }
        return entry.upcoming() ? "UPCOMING" : "PAST";
    }

    static String cancellationReasonOf(PatientHistoryEntry entry) {
        return entry.appointment().cancellation().map(Cancellation::reason).orElse(null);
    }
}
