package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/** Faixa de atendimento num dia da semana, em hora local do salão (ex.: segunda, 09:00 às 12:00). */
public final class WorkingPeriod {

    private final DayOfWeek dayOfWeek;
    private final LocalTime start;
    private final LocalTime end;

    public WorkingPeriod(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
        if (dayOfWeek == null || start == null || end == null) {
            throw new DomainException("Informe o dia da semana, o início e o fim do horário.");
        }
        if (!end.isAfter(start)) {
            throw new DomainException("O fim do horário deve ser depois do início.");
        }
        this.dayOfWeek = dayOfWeek;
        this.start = start;
        this.end = end;
    }

    boolean overlaps(WorkingPeriod other) {
        return dayOfWeek == other.dayOfWeek && start.isBefore(other.end) && other.start.isBefore(end);
    }

    boolean contains(DayOfWeek day, LocalTime from, LocalTime to) {
        return dayOfWeek == day && !from.isBefore(start) && !to.isAfter(end);
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof WorkingPeriod period
                && dayOfWeek == period.dayOfWeek && start.equals(period.start) && end.equals(period.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, start, end);
    }
}
