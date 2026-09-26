package co.edu.unicauca.piedrazul.appointments.infrastructure.web;

import co.edu.unicauca.piedrazul.appointments.application.BookAppointmentCommand;
import co.edu.unicauca.piedrazul.appointments.application.BookAppointmentService;
import co.edu.unicauca.piedrazul.appointments.application.BookingQueryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-03: agendamiento de citas por el propio paciente.
 */
@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasRole('PATIENT')")
public class BookingController {

    private final BookingQueryService bookingQueryService;
    private final BookAppointmentService bookAppointmentService;

    public BookingController(BookingQueryService bookingQueryService,
            BookAppointmentService bookAppointmentService) {
        this.bookingQueryService = bookingQueryService;
        this.bookAppointmentService = bookAppointmentService;
    }

    @GetMapping("/bookable-professionals")
    public List<BookableProfessionalResponse> bookableProfessionals(@RequestParam String specialty) {
        return bookingQueryService.professionalsFor(specialty).stream()
                .map(BookableProfessionalResponse::from)
                .toList();
    }

    @GetMapping("/bookable-professionals/{professionalId}/calendar")
    public BookingCalendarResponse calendar(@PathVariable UUID professionalId) {
        return BookingCalendarResponse.from(bookingQueryService.calendarOf(professionalId));
    }

    @GetMapping("/bookable-professionals/{professionalId}/slots")
    public DaySlotsResponse slots(@PathVariable UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return DaySlotsResponse.from(date, bookingQueryService.slotsOf(professionalId, date));
    }

    // El paciente sale del token y no de la petición: nadie puede agendar a nombre de otro.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookedAppointmentResponse book(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BookAppointmentRequest request) {
        BookAppointmentCommand command = new BookAppointmentCommand(UUID.fromString(jwt.getSubject()),
                request.professionalId(), request.date(), request.time());
        return BookedAppointmentResponse.from(bookAppointmentService.book(command));
    }
}
