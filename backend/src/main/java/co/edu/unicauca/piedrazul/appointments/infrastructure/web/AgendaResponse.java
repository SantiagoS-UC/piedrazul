package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.AgendaEntry;
import co.edu.unicauca.piedrazul.appointments.application.ProfessionalAgenda;
import co.edu.unicauca.piedrazul.identity.PatientSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AgendaResponse(
        BookableProfessionalResponse professional,
        LocalDate date,
        int total,
        List<AgendaAppointmentResponse> appointments) {

    /**
     * @param time hora de inicio en formato HH:mm
     */
    public record AgendaAppointmentResponse(UUID id, String time, int durationMinutes, AgendaPatientResponse patient) {
    }

    /**
     * Nombres y apellidos van por separado para que la tabla pueda ordenar por cualquiera de los dos.
     */
    public record AgendaPatientResponse(String givenNames, String lastNames, String documentType,
            String documentNumber, String phone) {

        // Si el registro del paciente ya no existe, la cita se muestra igual con este texto.
        static final AgendaPatientResponse UNKNOWN = new AgendaPatientResponse("", "Paciente no encontrado", "",
                "", "");

        static AgendaPatientResponse from(PatientSummary patient) {
            return new AgendaPatientResponse(patient.givenNames(), patient.lastNames(), patient.documentType(),
                    patient.documentNumber(), patient.phone());
        }
    }

    static AgendaResponse from(ProfessionalAgenda agenda) {
        List<AgendaAppointmentResponse> appointments = agenda.entries().stream()
                .map(AgendaResponse::toResponse)
                .toList();
        return new AgendaResponse(BookableProfessionalResponse.from(agenda.professional()), agenda.date(),
                agenda.total(), appointments);
    }

    private static AgendaAppointmentResponse toResponse(AgendaEntry entry) {
        AgendaPatientResponse patient = entry.patient()
                .map(AgendaPatientResponse::from)
                .orElse(AgendaPatientResponse.UNKNOWN);
        return new AgendaAppointmentResponse(entry.appointment().id(),
                TimeFormat.format(entry.appointment().startTime()), entry.appointment().durationMinutes(), patient);
    }
}
