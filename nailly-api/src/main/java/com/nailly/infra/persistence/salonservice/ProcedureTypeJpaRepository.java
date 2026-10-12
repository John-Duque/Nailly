package com.nailly.infra.persistence.salonservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedureTypeJpaRepository extends JpaRepository<ProcedureTypeEntity, Long> {

    List<ProcedureTypeEntity> findByActiveTrueOrderByName();
}
