package com.nailly.infra.persistence.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.shared.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class JpaAppointmentGateway implements AppointmentGateway {

    private final AppointmentJpaRepository repository;

    public JpaAppointmentGateway(AppointmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity;
        if (appointment.getId() == null) {
            entity = AppointmentMapper.toNewEntity(appointment);
        } else {
            entity = repository.findById(appointment.getId())
                    .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
            AppointmentMapper.copyChangeableFields(appointment, entity);
        }
        return AppointmentMapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(long id) {
        return repository.findById(id).map(AppointmentMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOccupyingSchedule(long professionalId, Instant startAt, Instant endAt, Instant now) {
        return repository.countOccupyingSchedule(professionalId, startAt, endAt, now,
                AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findPendingExpiredUntil(Instant now) {
        return toDomain(repository.findByStatusAndExpiresAtLessThanEqual(AppointmentStatus.PENDING, now));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByProfessionalBetween(long professionalId, Instant from, Instant to) {
        return toDomain(repository.findByProfessionalIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
                professionalId, to, from));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByCustomerBetween(long customerId, Instant from, Instant to) {
        return toDomain(repository.findByCustomerIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
                customerId, to, from));
    }

    private static List<Appointment> toDomain(List<AppointmentEntity> entities) {
        return entities.stream().map(AppointmentMapper::toDomain).toList();
    }
}
