package com.nailly.application.salonservice;

import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;

/**
 * A profissional tira um serviço do catálogo. Não é apagado do banco,
 * porque os itens de agendamentos antigos apontam para ele.
 */
public class DeactivateSalonServiceUseCase {

    private final SalonServiceGateway salonServiceGateway;

    public DeactivateSalonServiceUseCase(SalonServiceGateway salonServiceGateway) {
        this.salonServiceGateway = salonServiceGateway;
    }

    public SalonService execute(long professionalId, long serviceId) {
        SalonService service = salonServiceGateway.getByIdAndProfessional(serviceId, professionalId);
        service.deactivate();
        return salonServiceGateway.save(service);
    }
}
