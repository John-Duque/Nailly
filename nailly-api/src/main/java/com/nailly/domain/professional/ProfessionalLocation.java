package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Place;

/** Local onde a profissional atende (estabelecimento, estúdio etc.). */
public class ProfessionalLocation {

    private final Long id;
    private final long professionalId;
    private Place place;
    private boolean active = true;

    public ProfessionalLocation(Long id, long professionalId, Place place) {
        this.id = id;
        this.professionalId = professionalId;
        update(place);
    }

    public final void update(Place place) {
        if (place == null) {
            throw new DomainException("Informe os dados do local.");
        }
        this.place = place;
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

    public Place getPlace() {
        return place;
    }

    public boolean isActive() {
        return active;
    }
}
