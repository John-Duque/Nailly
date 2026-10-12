package com.nailly.application.salonservice;

import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;

/** Dados de um serviço enviados pela profissional, usados ao criar e ao editar. */
public final class SalonServiceCommand {

    private final long procedureTypeId;
    private final String title;
    private final String description;
    private final BodyArea bodyArea;
    private final int durationMinutes;
    private final Money price;
    private final boolean availableAtHome;

    public SalonServiceCommand(long procedureTypeId, String title, String description, BodyArea bodyArea,
                               int durationMinutes, Money price, boolean availableAtHome) {
        this.procedureTypeId = procedureTypeId;
        this.title = title;
        this.description = description;
        this.bodyArea = bodyArea;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.availableAtHome = availableAtHome;
    }

    ServiceDetails toDetails(ProcedureType procedureType) {
        return new ServiceDetails(procedureType, title, description, bodyArea, durationMinutes, price,
                availableAtHome);
    }

    public long getProcedureTypeId() {
        return procedureTypeId;
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

    public Money getPrice() {
        return price;
    }

    public boolean isAvailableAtHome() {
        return availableAtHome;
    }
}
