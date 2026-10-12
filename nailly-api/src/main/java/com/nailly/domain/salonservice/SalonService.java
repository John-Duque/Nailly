package com.nailly.domain.salonservice;

import com.nailly.domain.shared.DomainException;

/** Serviço oferecido por uma profissional. */
public class SalonService {

    private final Long id;
    private final long professionalId;
    private ServiceDetails details;
    private boolean active = true;

    public SalonService(Long id, long professionalId, ServiceDetails details) {
        this.id = id;
        this.professionalId = professionalId;
        update(details);
    }

    public final void update(ServiceDetails details) {
        if (details == null) {
            throw new DomainException("Informe os dados do serviço.");
        }
        this.details = details;
    }

    public void deactivate() {
        active = false;
    }

    public boolean belongsTo(long professionalId) {
        return this.professionalId == professionalId;
    }

    public Long getId() {
        return id;
    }

    public long getProfessionalId() {
        return professionalId;
    }

    public ServiceDetails getDetails() {
        return details;
    }

    public boolean isActive() {
        return active;
    }
}
