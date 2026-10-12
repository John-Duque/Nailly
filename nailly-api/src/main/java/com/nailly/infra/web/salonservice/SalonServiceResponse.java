package com.nailly.infra.web.salonservice;

import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;

import java.math.BigDecimal;

/** Serviço devolvido pela API. */
public class SalonServiceResponse {

    private final Long id;
    private final long professionalId;
    private final ProcedureTypeResponse procedureType;
    private final String title;
    private final String description;
    private final BodyArea bodyArea;
    private final int durationMinutes;
    private final BigDecimal price;
    private final boolean availableAtHome;
    private final boolean active;

    private SalonServiceResponse(SalonService service) {
        ServiceDetails details = service.getDetails();
        this.id = service.getId();
        this.professionalId = service.getProfessionalId();
        this.procedureType = ProcedureTypeResponse.from(details.getProcedureType());
        this.title = details.getTitle();
        this.description = details.getDescription();
        this.bodyArea = details.getBodyArea();
        this.durationMinutes = details.getDurationMinutes();
        this.price = details.getPrice().getAmount();
        this.availableAtHome = details.isAvailableAtHome();
        this.active = service.isActive();
    }

    public static SalonServiceResponse from(SalonService service) {
        return new SalonServiceResponse(service);
    }

    public Long getId() {
        return id;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public ProcedureTypeResponse getProcedureType() {
        return procedureType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BodyArea getBodyArea() {
        return bodyArea;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isAvailableAtHome() {
        return availableAtHome;
    }

    public boolean isActive() {
        return active;
    }
}
