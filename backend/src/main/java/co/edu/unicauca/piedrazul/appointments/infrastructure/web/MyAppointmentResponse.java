package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.PatientHistoryEntry;
import co.edu.unicauca.piedrazul.professionals.ProfessionalSummary;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Una cita del paciente tal como la ve él mismo.
 *
 * @param time               hora de inicio en formato HH:mm
 * @param status             UPCOMING, PAST o CANCELLED
 * @param cancellable        si todavía puede cancelarla (mínimo dos días antes)
 * @param cancellationReason {@code null} si la cita no se canceló
 */
public record MyAppointmentResponse(UUID id, LocalDate date, String time, int durationMinutes,
        String professionalName, String specialtyName, String status, boolean cancellable,
        String cancellationReason) {

    static MyAppointmentResponse from(PatientHistoryEntry entry) {
        return new MyAppointmentResponse(entry.appointment().id(), entry.appointment().date(),
                TimeFormat.format(entry.appointment().startTime()), entry.appointment().durationMinutes(),
                entry.professional().map(ProfessionalSummary::fullName).orElse("Profesional no encontrado"),
                entry.professional().map(ProfessionalSummary::specialtyName).orElse(""),
                PatientHistoryResponse.statusOf(entry), entry.cancellable(),
                PatientHistoryResponse.cancellationReasonOf(entry));
    }
}
