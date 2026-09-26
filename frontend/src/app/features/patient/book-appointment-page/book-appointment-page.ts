import { HttpErrorResponse } from '@angular/common/http';
import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  inject,
  Injector,
  signal,
  viewChild,
} from '@angular/core';
import { finalize } from 'rxjs';
import { toApiError } from '../../../core/http/api-error';
import {
  lastNameFirst,
  ProfessionalsApi,
  Specialty,
  SpecialtyCode,
} from '../../../core/professionals/professionals-api';
import { formatTime, formatWeekdayDate, parseIsoDate } from '../../../shared/format/date-time-format';
import { Button } from '../../../shared/ui/atoms/button/button';
import { Icon } from '../../../shared/ui/atoms/icon/icon';
import { Alert } from '../../../shared/ui/molecules/alert/alert';
import { ChoiceButton } from '../../../shared/ui/molecules/choice-button/choice-button';
import { PageHeader } from '../../../shared/ui/molecules/page-header/page-header';
import { SlotOption, SlotPicker } from '../../../shared/ui/molecules/slot-picker/slot-picker';
import { StepIndicator } from '../../../shared/ui/molecules/step-indicator/step-indicator';
import { MonthCalendar } from '../../../shared/ui/organisms/month-calendar/month-calendar';
import { BookableProfessional, BookedAppointment, BookingApi, BookingCalendar, worksOn } from '../booking-api';
import { BookingSummary } from '../organisms/booking-summary/booking-summary';

const STEPS = ['Especialidad', 'Profesional', 'Fecha y hora', 'Confirmación'];

const SPECIALTY_DETAIL: Record<SpecialtyCode, string> = {
  GENERAL_MEDICINE: 'Consulta con médico',
  NEURAL_THERAPY: 'Sesión con terapista',
  CHIROPRACTIC: 'Sesión con terapista',
  PHYSIOTHERAPY: 'Sesión con terapista',
};

/**
 * HU-03: el paciente agenda su cita en cuatro pasos. Volver a un paso anterior conserva lo elegido;
 * cambiar una elección borra solo las que dependen de ella.
 */
@Component({
  selector: 'app-book-appointment-page',
  imports: [Alert, Button, BookingSummary, ChoiceButton, Icon, MonthCalendar, PageHeader, SlotPicker, StepIndicator],
  templateUrl: './book-appointment-page.html',
  styleUrl: './book-appointment-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookAppointmentPage {
  private readonly bookingApi = inject(BookingApi);
  private readonly injector = inject(Injector);
  private readonly stepHeading = viewChild<ElementRef<HTMLElement>>('stepHeading');

  protected readonly steps = STEPS;
  protected readonly specialtyDetail = SPECIALTY_DETAIL;
  protected readonly lastNameFirst = lastNameFirst;
  protected readonly formatTime = formatTime;
  protected readonly weekdayDate = (iso: string) => formatWeekdayDate(parseIsoDate(iso));

  protected readonly step = signal(0);
  protected readonly specialties = signal<Specialty[]>([]);
  protected readonly specialty = signal<Specialty | null>(null);
  protected readonly professionals = signal<BookableProfessional[]>([]);
  protected readonly professional = signal<BookableProfessional | null>(null);
  protected readonly calendar = signal<BookingCalendar | null>(null);
  protected readonly date = signal<string | null>(null);
  protected readonly slots = signal<SlotOption[]>([]);
  protected readonly time = signal<string | null>(null);
  protected readonly booked = signal<BookedAppointment | null>(null);

  protected readonly loading = signal(false);
  protected readonly loadingSlots = signal(false);
  protected readonly booking = signal(false);
  /** Falta elegir algo para avanzar. */
  protected readonly stepError = signal<string | null>(null);
  /** Error que devolvió el servidor. */
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly isDateEnabled = computed(() => {
    const calendar = this.calendar();
    return (iso: string) => calendar !== null && worksOn(calendar, iso);
  });
  protected readonly longDate = computed(() => {
    const date = this.date();
    return date ? this.weekdayDate(date) : '';
  });
  protected readonly noFreeSlots = computed(
    () => !this.loadingSlots() && this.slots().length > 0 && this.slots().every((slot) => !slot.available),
  );

  constructor() {
    inject(ProfessionalsApi)
      .getSpecialties()
      .subscribe({
        next: (specialties) => this.specialties.set(specialties),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  protected chooseSpecialty(specialty: Specialty): void {
    if (this.specialty()?.code !== specialty.code) {
      this.specialty.set(specialty);
      this.professionals.set([]);
      this.chooseProfessional(null);
    }
    this.stepError.set(null);
  }

  protected chooseProfessional(professional: BookableProfessional | null): void {
    if (this.professional()?.id !== professional?.id) {
      this.professional.set(professional);
      this.calendar.set(null);
      this.date.set(null);
      this.slots.set([]);
      this.time.set(null);
    }
    this.stepError.set(null);
  }

  protected chooseDate(date: string): void {
    this.date.set(date);
    this.time.set(null);
    this.stepError.set(null);
    this.errorMessage.set(null);
    this.loadSlots();
  }

  protected chooseTime(time: string): void {
    this.time.set(time);
    this.stepError.set(null);
  }

  protected next(): void {
    const missing = this.missingChoice();
    if (missing) {
      this.stepError.set(missing);
      return;
    }
    const nextStep = this.step() + 1;
    if (nextStep === 1 && this.professionals().length === 0) {
      this.loadProfessionals();
    }
    if (nextStep === 2 && !this.calendar()) {
      this.loadCalendar();
    }
    this.goTo(nextStep);
  }

  protected previous(): void {
    this.goTo(this.step() - 1);
  }

  protected confirm(): void {
    const professional = this.professional();
    const date = this.date();
    const time = this.time();
    if (!professional || !date || !time) {
      return;
    }
    this.booking.set(true);
    this.errorMessage.set(null);
    this.bookingApi
      .book(professional.id, date, time)
      .pipe(finalize(() => this.booking.set(false)))
      .subscribe({
        next: (booked) => {
          this.booked.set(booked);
          this.focusHeading();
        },
        error: (error: unknown) => this.handleBookingError(error),
      });
  }

  protected startOver(): void {
    this.booked.set(null);
    this.specialty.set(null);
    this.professionals.set([]);
    this.chooseProfessional(null);
    this.goTo(0);
  }

  private missingChoice(): string | null {
    switch (this.step()) {
      case 0:
        return this.specialty() ? null : 'Elige una especialidad para continuar.';
      case 1:
        return this.professional() ? null : 'Elige un profesional para continuar.';
      case 2:
        if (!this.date()) {
          return 'Elige una fecha en el calendario para continuar.';
        }
        return this.time() ? null : 'Elige una hora para continuar.';
      default:
        return null;
    }
  }

  // Si el horario ya no está libre o dejó de ser válido, se vuelve a la elección de hora con las
  // franjas actualizadas para que el paciente elija otra sin empezar de nuevo.
  private handleBookingError(error: unknown): void {
    const apiError = toApiError(error);
    this.errorMessage.set(apiError.message);
    const slotProblem =
      error instanceof HttpErrorResponse &&
      (error.status === 409 || 'date' in apiError.fieldErrors || 'time' in apiError.fieldErrors);
    if (slotProblem) {
      this.time.set(null);
      this.goTo(2, false);
      this.loadSlots();
    }
  }

  private loadProfessionals(): void {
    const specialty = this.specialty();
    if (!specialty) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.bookingApi
      .getProfessionals(specialty.code)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (professionals) => this.professionals.set(professionals),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  private loadCalendar(): void {
    const professional = this.professional();
    if (!professional) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.bookingApi
      .getCalendar(professional.id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (calendar) => this.calendar.set(calendar),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  private loadSlots(): void {
    const professional = this.professional();
    const date = this.date();
    if (!professional || !date) {
      return;
    }
    this.loadingSlots.set(true);
    this.slots.set([]);
    this.bookingApi
      .getSlots(professional.id, date)
      .pipe(finalize(() => this.loadingSlots.set(false)))
      .subscribe({
        next: (daySlots) => this.slots.set(daySlots.slots),
        error: (error: unknown) => this.errorMessage.set(toApiError(error).message),
      });
  }

  private goTo(step: number, clearMessages = true): void {
    this.step.set(step);
    this.stepError.set(null);
    if (clearMessages) {
      this.errorMessage.set(null);
    }
    this.focusHeading();
  }

  // Al cambiar de paso, el foco va al título para que el lector de pantalla anuncie dónde está.
  private focusHeading(): void {
    afterNextRender(() => this.stepHeading()?.nativeElement.focus(), { injector: this.injector });
  }
}
