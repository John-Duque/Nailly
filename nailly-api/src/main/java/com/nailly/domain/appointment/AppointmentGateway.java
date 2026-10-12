package com.nailly.domain.appointment;

import com.nailly.domain.shared.NotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Acesso aos agendamentos gravados. Implementado na camada infra. */
public interface AppointmentGateway {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(long id);

    default Appointment getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
    }

    /**
     * Se a profissional tem algo ocupando a agenda em [startAt, endAt): agendamento CONFIRMED
     * ou pedido PENDING ainda no prazo em {@code now} (sem expires_at ou com expires_at depois de now).
     */
    boolean existsOccupyingSchedule(long professionalId, Instant startAt, Instant endAt, Instant now);

    /** Pedidos PENDING com {@code expires_at} até {@code now}, para a rotina de expiração. */
    List<Appointment> findPendingExpiredUntil(Instant now);
}
