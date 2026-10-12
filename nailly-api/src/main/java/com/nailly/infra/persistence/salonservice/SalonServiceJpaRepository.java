package com.nailly.infra.persistence.salonservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonServiceJpaRepository extends JpaRepository<SalonServiceEntity, Long> {

    List<SalonServiceEntity> findByProfessionalIdAndActiveTrueOrderByTitle(long professionalId);
}
