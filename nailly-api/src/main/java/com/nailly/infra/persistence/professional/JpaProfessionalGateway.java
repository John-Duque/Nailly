package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class JpaProfessionalGateway implements ProfessionalGateway {

    private final ProfessionalJpaRepository repository;

    public JpaProfessionalGateway(ProfessionalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Professional> findById(long id) {
        return repository.findById(id).map(ProfessionalMapper::toDomain);
    }

    @Override
    public Optional<Professional> findByIdForUpdate(long id) {
        return repository.findByIdForUpdate(id).map(ProfessionalMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public Professional save(Professional professional) {
        ProfessionalEntity entity = professional.getId() == null
                ? new ProfessionalEntity(professional.getUserId())
                : repository.findById(professional.getId())
                        .orElseThrow(() -> new NotFoundException("Profissional não encontrada."));
        ProfessionalMapper.copyToEntity(professional, entity);
        return ProfessionalMapper.toDomain(repository.save(entity));
    }
}
