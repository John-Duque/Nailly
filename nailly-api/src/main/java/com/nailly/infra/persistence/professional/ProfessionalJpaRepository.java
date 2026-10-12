package com.nailly.infra.persistence.professional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, Long> {

    /** SELECT ... FOR UPDATE: a linha fica travada até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfessionalEntity p where p.id = :id")
    Optional<ProfessionalEntity> findByIdForUpdate(@Param("id") long id);

    boolean existsByUserId(long userId);
}
