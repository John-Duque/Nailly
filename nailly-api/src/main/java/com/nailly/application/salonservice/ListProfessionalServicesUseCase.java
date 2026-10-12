package com.nailly.application.salonservice;

import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;

import java.util.List;

/** Catálogo da profissional: os serviços ativos que a cliente pode agendar. */
public class ListProfessionalServicesUseCase {

    private final SalonServiceGateway salonServiceGateway;

    public ListProfessionalServicesUseCase(SalonServiceGateway salonServiceGateway) {
        this.salonServiceGateway = salonServiceGateway;
    }

    public List<SalonService> execute(long professionalId) {
        return salonServiceGateway.findActiveByProfessional(professionalId);
    }
}
