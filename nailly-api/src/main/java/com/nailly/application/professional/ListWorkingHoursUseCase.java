package com.nailly.application.professional;

import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;

import java.util.List;

/** Horários de atendimento da profissional em cada local e a domicílio. */
public class ListWorkingHoursUseCase {

    private final WorkingHoursGateway workingHoursGateway;

    public ListWorkingHoursUseCase(WorkingHoursGateway workingHoursGateway) {
        this.workingHoursGateway = workingHoursGateway;
    }

    public List<WorkingHours> execute(long professionalId) {
        return workingHoursGateway.findAllByProfessional(professionalId);
    }
}
