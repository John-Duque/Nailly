package com.nailly.infra.persistence.salonservice;

import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.ProcedureTypeGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaProcedureTypeGateway implements ProcedureTypeGateway {

    private final ProcedureTypeJpaRepository repository;

    public JpaProcedureTypeGateway(ProcedureTypeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProcedureType> findById(long id) {
        return repository.findById(id).map(SalonServiceMapper::toDomain);
    }

    @Override
    public List<ProcedureType> findAllActive() {
        return repository.findByActiveTrueOrderByName().stream().map(SalonServiceMapper::toDomain).toList();
    }
}
