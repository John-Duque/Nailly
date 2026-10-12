package com.nailly.domain.salonservice;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.DomainValidation;
import com.nailly.domain.shared.Money;

/** O que a profissional informa ao cadastrar ou editar um serviço. */
public final class ServiceDetails {

    /** Um serviço não passa de 24 horas (e cabe no SMALLINT do banco). */
    public static final int MAX_DURATION_MINUTES = 24 * 60;

    private final ProcedureType procedureType;
    private final String title;
    private final String description;
    private final BodyArea bodyArea;
    private final int durationMinutes;
    private final Money price;
    private final boolean availableAtHome;

    /** @param availableAtHome {@code false} = serviço que ela não faz na casa da cliente */
    public ServiceDetails(ProcedureType procedureType, String title, String description, BodyArea bodyArea,
                          int durationMinutes, Money price, boolean availableAtHome) {
        if (procedureType == null) {
            throw new DomainException("Informe a técnica do serviço.");
        }
        if (bodyArea == null) {
            throw new DomainException("Informe a região do serviço (mãos, pés ou ambos).");
        }
        if (durationMinutes <= 0) {
            throw new DomainException("A duração do serviço deve ser maior que zero.");
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw new DomainException("A duração do serviço deve ser de no máximo 24 horas.");
        }
        if (price == null) {
            throw new DomainException("Informe o preço do serviço.");
        }
        this.procedureType = procedureType;
        this.title = DomainValidation.requireText(title, "Informe o título do serviço.");
        this.description = DomainValidation.optionalText(description);
        this.bodyArea = bodyArea;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.availableAtHome = availableAtHome;
    }

    public ProcedureType getProcedureType() {
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

    public Money getPrice() {
        return price;
    }

    public boolean isAvailableAtHome() {
        return availableAtHome;
    }
}
