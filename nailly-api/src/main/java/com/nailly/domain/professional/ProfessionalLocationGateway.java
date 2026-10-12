package com.nailly.domain.professional;

import com.nailly.domain.shared.NotFoundException;

import java.util.List;
import java.util.Optional;

/** Acesso aos locais de atendimento. Implementado na camada infra. */
public interface ProfessionalLocationGateway {

    Optional<ProfessionalLocation> findById(long id);

    /** Local da profissional informada; local de outra profissional também responde "não encontrado". */
    default ProfessionalLocation getByIdAndProfessional(long id, long professionalId) {
        return findById(id)
                .filter(location -> location.belongsTo(professionalId))
                .orElseThrow(() -> new NotFoundException("Local não encontrado."));
    }

    List<ProfessionalLocation> findActiveByProfessional(long professionalId);

    ProfessionalLocation save(ProfessionalLocation location);
}
