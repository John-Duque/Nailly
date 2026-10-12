package com.nailly.infra.persistence.professional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

/**
 * Linha de working_hours: um período de um dia da semana, em hora local do salão.
 * day_of_week segue java.time.DayOfWeek (1 = segunda ... 7 = domingo); location_id nulo = domicílio.
 */
@Entity
@Table(name = "working_hours")
public class WorkingHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professional_id", nullable = false)
    private long professionalId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "day_of_week", nullable = false)
    private byte dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected WorkingHoursEntity() {
    }

    public WorkingHoursEntity(long professionalId, Long locationId, byte dayOfWeek, LocalTime startTime,
                              LocalTime endTime) {
        this.professionalId = professionalId;
        this.locationId = locationId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getLocationId() {
        return locationId;
    }

    public byte getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
