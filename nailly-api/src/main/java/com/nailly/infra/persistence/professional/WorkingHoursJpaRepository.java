package com.nailly.infra.persistence.professional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkingHoursJpaRepository extends JpaRepository<WorkingHoursEntity, Long> {

    List<WorkingHoursEntity> findByProfessionalIdAndLocationId(long professionalId, long locationId);

    List<WorkingHoursEntity> findByProfessionalIdAndLocationIdIsNull(long professionalId);

    List<WorkingHoursEntity> findByProfessionalIdOrderByLocationIdAscDayOfWeekAscStartTimeAsc(long professionalId);
}
