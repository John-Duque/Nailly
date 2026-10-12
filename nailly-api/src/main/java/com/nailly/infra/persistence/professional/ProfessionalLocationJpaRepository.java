package com.nailly.infra.persistence.professional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalLocationJpaRepository extends JpaRepository<ProfessionalLocationEntity, Long> {

    List<ProfessionalLocationEntity> findByProfessionalIdAndActiveTrueOrderByName(long professionalId);
}
