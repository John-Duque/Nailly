package com.nailly.infra.web.professional;

import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingPeriod;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** Horários de um local (ou a domicílio, com {@code locationId} nulo), em hora local do salão. */
public class WorkingHoursResponse {

    private final Long locationId;
    private final boolean homeVisit;
    private final List<Period> periods;

    private WorkingHoursResponse(WorkingHours workingHours) {
        this.locationId = workingHours.getLocationId();
        this.homeVisit = workingHours.isHomeVisit();
        this.periods = workingHours.getPeriods().stream().map(Period::new).toList();
    }

    public static WorkingHoursResponse from(WorkingHours workingHours) {
        return new WorkingHoursResponse(workingHours);
    }

    public Long getLocationId() {
        return locationId;
    }

    public boolean isHomeVisit() {
        return homeVisit;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public static class Period {

        private final DayOfWeek dayOfWeek;
        private final LocalTime start;
        private final LocalTime end;

        Period(WorkingPeriod period) {
            this.dayOfWeek = period.getDayOfWeek();
            this.start = period.getStart();
            this.end = period.getEnd();
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
    }
}
