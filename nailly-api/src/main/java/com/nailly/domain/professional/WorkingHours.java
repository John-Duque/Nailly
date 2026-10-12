package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.SalonTime;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Horário de funcionamento semanal da profissional em um local, ou a domicílio
 * ({@code locationId} nulo). Vários períodos no mesmo dia formam intervalos (ex.: 09-12 e 13-18).
 */
public class WorkingHours {

    private final long professionalId;
    private final Long locationId;
    private final List<WorkingPeriod> periods;

    public WorkingHours(long professionalId, Long locationId, List<WorkingPeriod> periods) {
        List<WorkingPeriod> sorted = periods == null ? List.of() : periods.stream()
                .sorted(Comparator.comparing(WorkingPeriod::getDayOfWeek).thenComparing(WorkingPeriod::getStart))
                .toList();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).overlaps(sorted.get(i - 1))) {
                throw new DomainException("Há horários sobrepostos no mesmo dia.");
            }
        }
        this.professionalId = professionalId;
        this.locationId = locationId;
        this.periods = sorted;
    }

    /** Se o atendimento de {@code start} a {@code end} (UTC) cabe inteiro em um período, no mesmo dia. */
    public boolean covers(Instant start, Instant end) {
        ZonedDateTime localStart = start.atZone(SalonTime.ZONE);
        ZonedDateTime localEnd = end.atZone(SalonTime.ZONE);
        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            return false;
        }
        return periods.stream().anyMatch(period -> period.contains(localStart.getDayOfWeek(),
                localStart.toLocalTime(), localEnd.toLocalTime()));
    }

    public boolean isHomeVisit() {
        return locationId == null;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public List<WorkingPeriod> getPeriods() {
        return periods;
    }
}
