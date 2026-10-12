package com.nailly.infra.scheduling;

import com.nailly.application.appointment.ExpirePendingAppointmentsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Expira periodicamente os pedidos PENDING que a profissional não respondeu a tempo.
 * Liga/desliga e intervalo no application.properties (nailly.scheduling); os testes desligam.
 */
@Component
@EnableScheduling
@ConditionalOnBooleanProperty("nailly.scheduling.enabled")
public class AppointmentExpirationJob {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentExpirationJob.class);

    private final ExpirePendingAppointmentsUseCase expirePendingAppointments;

    public AppointmentExpirationJob(ExpirePendingAppointmentsUseCase expirePendingAppointments) {
        this.expirePendingAppointments = expirePendingAppointments;
    }

    @Scheduled(fixedDelayString = "${nailly.scheduling.expire-pending-delay}")
    public void expirePendingAppointments() {
        int expired = expirePendingAppointments.execute();
        if (expired > 0) {
            LOG.info("{} pedido(s) pendente(s) expirado(s)", expired);
        }
    }
}
