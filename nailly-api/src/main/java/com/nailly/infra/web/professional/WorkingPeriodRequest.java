package com.nailly.infra.web.professional;

import com.nailly.domain.professional.WorkingPeriod;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Um período do dia, em hora local do salão. Ex.: {"dayOfWeek": "MONDAY", "start": "09:00", "end": "12:00"}. */
public class WorkingPeriodRequest {

    @NotNull(message = "Informe o dia da semana (MONDAY a SUNDAY).")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Informe o início (HH:mm).")
    private LocalTime start;

    @NotNull(message = "Informe o fim (HH:mm).")
    private LocalTime end;

    WorkingPeriod toDomain() {
        return new WorkingPeriod(dayOfWeek, start, end);
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}
