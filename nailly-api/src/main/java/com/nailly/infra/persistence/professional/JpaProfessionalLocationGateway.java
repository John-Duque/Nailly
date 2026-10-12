package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.infra.persistence.shared.AddressColumns;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class JpaProfessionalLocationGateway implements ProfessionalLocationGateway {

    private final ProfessionalLocationJpaRepository repository;

    public JpaProfessionalLocationGateway(ProfessionalLocationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfessionalLocation> findById(long id) {
        return repository.findById(id).map(JpaProfessionalLocationGateway::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalLocation> findActiveByProfessional(long professionalId) {
        return repository.findByProfessionalIdAndActiveTrueOrderByName(professionalId).stream()
                .map(JpaProfessionalLocationGateway::toDomain)
                .toList();
    }

    @Override
    public ProfessionalLocation save(ProfessionalLocation location) {
        ProfessionalLocationEntity entity = location.getId() == null
                ? new ProfessionalLocationEntity(location.getProfessionalId())
                : repository.findById(location.getId())
                        .orElseThrow(() -> new NotFoundException("Local não encontrado."));
        entity.setName(location.getPlace().getName());
        entity.setAddress(AddressColumns.from(location.getPlace()));
        entity.setActive(location.isActive());
        return toDomain(repository.save(entity));
    }

    private static ProfessionalLocation toDomain(ProfessionalLocationEntity entity) {
        ProfessionalLocation location = new ProfessionalLocation(entity.getId(), entity.getProfessionalId(),
                entity.getAddress().toPlace(entity.getName()));
        if (!entity.isActive()) {
            location.deactivate();
        }
        return location;
    }
}
