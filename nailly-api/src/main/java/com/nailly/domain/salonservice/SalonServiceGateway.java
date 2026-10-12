package com.nailly.domain.salonservice;

import com.nailly.domain.shared.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Acesso aos serviços gravados. Implementado na camada infra. */
public interface SalonServiceGateway {

    Optional<SalonService> findById(long id);

    /**
     * Serviço da profissional informada. Serviço de outra profissional também
     * responde "não encontrado", para não revelar o que é de outra pessoa.
     */
    default SalonService getByIdAndProfessional(long id, long professionalId) {
        return findById(id)
                .filter(service -> service.belongsTo(professionalId))
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado."));
    }

    /** Serviços com os ids informados; ids inexistentes ficam de fora da lista. */
    List<SalonService> findAllByIds(Collection<Long> ids);

    List<SalonService> findActiveByProfessional(long professionalId);

    SalonService save(SalonService service);
}
