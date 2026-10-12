package com.nailly.infra.persistence.salonservice;

import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.shared.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class JpaSalonServiceGateway implements SalonServiceGateway {

    private final SalonServiceJpaRepository repository;
    private final ProcedureTypeJpaRepository procedureTypeRepository;

    public JpaSalonServiceGateway(SalonServiceJpaRepository repository,
                                  ProcedureTypeJpaRepository procedureTypeRepository) {
        this.repository = repository;
        this.procedureTypeRepository = procedureTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalonService> findById(long id) {
        return repository.findById(id).map(SalonServiceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalonService> findAllByIds(Collection<Long> ids) {
        return repository.findAllById(ids).stream().map(SalonServiceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalonService> findActiveByProfessional(long professionalId) {
        return repository.findByProfessionalIdAndActiveTrueOrderByTitle(professionalId).stream()
                .map(SalonServiceMapper::toDomain)
                .toList();
    }

    @Override
    public SalonService save(SalonService service) {
        SalonServiceEntity entity = service.getId() == null
                ? new SalonServiceEntity(service.getProfessionalId())
                : repository.findById(service.getId())
                        .orElseThrow(() -> new NotFoundException("Serviço não encontrado."));
        SalonServiceMapper.copyToEntity(service, entity);
        entity.setProcedureType(procedureTypeRepository.getReferenceById(
                service.getDetails().getProcedureType().getId()));
        return SalonServiceMapper.toDomain(repository.save(entity));
    }
}
