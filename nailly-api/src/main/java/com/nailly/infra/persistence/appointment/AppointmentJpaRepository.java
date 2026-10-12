package com.nailly.infra.persistence.appointment;

import com.nailly.domain.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {

    /** Agendamentos que se sobrepõem a [startAt, endAt): CONFIRMED, ou PENDING ainda no prazo. */
    @Query("""
            select count(a) from AppointmentEntity a
            where a.professionalId = :professionalId
              and a.startAt < :endAt
              and a.endAt > :startAt
              and (a.status = :confirmed
                   or (a.status = :pending and (a.expiresAt is null or a.expiresAt > :now)))
            """)
    long countOccupyingSchedule(@Param("professionalId") long professionalId,
                                @Param("startAt") Instant startAt,
                                @Param("endAt") Instant endAt,
                                @Param("now") Instant now,
                                @Param("confirmed") AppointmentStatus confirmed,
                                @Param("pending") AppointmentStatus pending);

    List<AppointmentEntity> findByStatusAndExpiresAtLessThanEqual(AppointmentStatus status, Instant expiresAt);

    /** Agendamentos que tocam o período: começam antes do fim dele e terminam depois do início. */
    List<AppointmentEntity> findByProfessionalIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
            long professionalId, Instant periodEnd, Instant periodStart);

    List<AppointmentEntity> findByCustomerIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
            long customerId, Instant periodEnd, Instant periodStart);
}
