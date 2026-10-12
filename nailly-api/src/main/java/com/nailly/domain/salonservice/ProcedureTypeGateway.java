package com.nailly.domain.salonservice;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;

import java.util.List;
import java.util.Optional;

/** Acesso às técnicas cadastradas. Implementado na camada infra. */
public interface ProcedureTypeGateway {

    Optional<ProcedureType> findById(long id);

    /** Técnica que pode ser usada em um serviço novo ou editado. */
    default ProcedureType getActiveById(long id) {
        ProcedureType procedureType = findById(id)
                .orElseThrow(() -> new NotFoundException("Técnica não encontrada."));
        if (!procedureType.isActive()) {
            throw new DomainException("A técnica " + procedureType.getName() + " não está mais disponível.");
        }
        return procedureType;
    }

    /** Técnicas ativas, em ordem alfabética. */
    List<ProcedureType> findAllActive();
}
