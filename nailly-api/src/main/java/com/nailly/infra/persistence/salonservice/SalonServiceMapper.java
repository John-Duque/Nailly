package com.nailly.infra.persistence.salonservice;

import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;

/** Converte técnicas e serviços entre o domínio e as entidades do banco. */
final class SalonServiceMapper {

    private SalonServiceMapper() {
    }

    static ProcedureType toDomain(ProcedureTypeEntity entity) {
        return new ProcedureType(entity.getId(), entity.getName(), entity.isActive());
    }

    static SalonService toDomain(SalonServiceEntity entity) {
        ServiceDetails details = new ServiceDetails(toDomain(entity.getProcedureType()), entity.getTitle(),
                entity.getDescription(), entity.getBodyArea(), entity.getDurationMinutes(),
                new Money(entity.getPrice()), entity.isAvailableAtHome());
        SalonService service = new SalonService(entity.getId(), entity.getProfessionalId(), details);
        if (!entity.isActive()) {
            service.deactivate();
        }
        return service;
    }

    /** Copia os dados do domínio; a técnica é ligada à parte, porque precisa da referência JPA. */
    static void copyToEntity(SalonService service, SalonServiceEntity entity) {
        ServiceDetails details = service.getDetails();
        entity.setTitle(details.getTitle());
        entity.setDescription(details.getDescription());
        entity.setBodyArea(details.getBodyArea());
        // Cabe no SMALLINT: o domínio limita a duração a ServiceDetails.MAX_DURATION_MINUTES.
        entity.setDurationMinutes((short) details.getDurationMinutes());
        entity.setPrice(details.getPrice().getAmount());
        entity.setAvailableAtHome(details.isAvailableAtHome());
        entity.setActive(service.isActive());
    }
}
