package com.nailly.domain.professional;

import java.util.List;

/** Acesso aos horários de funcionamento. Implementado na camada infra. */
public interface WorkingHoursGateway {

    /** Horários do local ({@code locationId}) ou a domicílio ({@code null}); vazio se não houver. */
    WorkingHours find(long professionalId, Long locationId);

    /** Todos os horários da profissional, agrupados por local (domicílio incluído). */
    List<WorkingHours> findAllByProfessional(long professionalId);

    /** Substitui os horários daquele local (ou do domicílio) pelos informados. */
    WorkingHours save(WorkingHours workingHours);
}
