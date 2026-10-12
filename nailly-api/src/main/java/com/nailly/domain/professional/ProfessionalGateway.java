package com.nailly.domain.professional;

import com.nailly.domain.shared.NotFoundException;

import java.util.Optional;

/** Acesso às profissionais gravadas. Implementado na camada infra. */
public interface ProfessionalGateway {

    Optional<Professional> findById(long id);

    default Professional getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Profissional não encontrada."));
    }

    /**
     * Busca e trava a profissional até o fim da transação (SELECT ... FOR UPDATE).
     * Usado ao agendar: dois pedidos simultâneos para ela esperam um ao outro,
     * evitando dois agendamentos no mesmo horário.
     */
    Optional<Professional> findByIdForUpdate(long id);

    Optional<Professional> findByUserId(long userId);

    Professional save(Professional professional);
}
