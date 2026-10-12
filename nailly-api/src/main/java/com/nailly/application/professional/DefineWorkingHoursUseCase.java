package com.nailly.application.professional;

import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.shared.DomainException;

import java.util.List;

/**
 * A profissional define a semana de atendimento de um local, ou a domicílio ({@code locationId} nulo).
 * Substitui os horários anteriores daquele local; lista vazia = não atende lá.
 */
public class DefineWorkingHoursUseCase {

    private final ProfessionalGateway professionalGateway;
    private final ProfessionalLocationGateway locationGateway;
    private final WorkingHoursGateway workingHoursGateway;

    public DefineWorkingHoursUseCase(ProfessionalGateway professionalGateway,
                                     ProfessionalLocationGateway locationGateway,
                                     WorkingHoursGateway workingHoursGateway) {
        this.professionalGateway = professionalGateway;
        this.locationGateway = locationGateway;
        this.workingHoursGateway = workingHoursGateway;
    }

    public WorkingHours execute(long professionalId, Long locationId, List<WorkingPeriod> periods) {
        professionalGateway.getById(professionalId);
        if (locationId != null) {
            ProfessionalLocation location = locationGateway.getByIdAndProfessional(locationId, professionalId);
            if (!location.isActive()) {
                throw new DomainException("Este local está desativado.");
            }
        }
        return workingHoursGateway.save(new WorkingHours(professionalId, locationId, periods));
    }
}
