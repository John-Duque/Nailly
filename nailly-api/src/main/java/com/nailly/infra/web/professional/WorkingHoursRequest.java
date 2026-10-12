package com.nailly.infra.web.professional;

import com.nailly.domain.professional.WorkingPeriod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Semana de atendimento de um local ({@code locationId}) ou a domicílio ({@code locationId} vazio).
 * Substitui os horários anteriores daquele local; lista vazia = não atende lá.
 */
public class WorkingHoursRequest {

    @Positive(message = "Local inválido.")
    private Long locationId;

    @NotNull(message = "Informe os períodos (pode ser uma lista vazia).")
    @Valid
    private List<@NotNull(message = "Período vazio.") WorkingPeriodRequest> periods;

    Long getLocationId() {
        return locationId;
    }

    List<WorkingPeriod> toPeriods() {
        return periods.stream().map(WorkingPeriodRequest::toDomain).toList();
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setPeriods(List<WorkingPeriodRequest> periods) {
        this.periods = periods;
    }
}
