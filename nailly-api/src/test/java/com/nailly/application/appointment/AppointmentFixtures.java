package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Dados de exemplo usados nos testes dos casos de uso. */
final class AppointmentFixtures {

    static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant START = Instant.parse("2026-10-13T13:00:00Z");
    static final long CUSTOMER_ID = 20L;
    static final long PROFESSIONAL_ID = 10L;
    static final GeoPoint ORIGIN = new GeoPoint(0, 0);
    static final ServiceLocation SALON = ServiceLocation.atLocation(3L, "Rua A, 10");

    private static final ProcedureType GEL = new ProcedureType(2L, "Esmaltação em gel", true);

    private AppointmentFixtures() {
    }

    static Clock clockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    static Professional professional(ApprovalMode approval) {
        HomeVisitPolicy homeVisit = new HomeVisitPolicy(true, ORIGIN,
                List.of(new HomeVisitFee(new BigDecimal("5"), Money.of("10")),
                        new HomeVisitFee(new BigDecimal("10"), Money.of("20"))));
        return new Professional(PROFESSIONAL_ID, 1L, "Ana", null, null,
                new ApprovalPolicy(approval, approval, 30), homeVisit);
    }

    static SalonService service(long id, int minutes, String price) {
        return new SalonService(id, PROFESSIONAL_ID, new ServiceDetails(GEL, "Serviço " + id, null,
                BodyArea.HANDS, minutes, Money.of(price), true));
    }

    /** Pedido PENDING feito em NOW, com prazo de resposta até NOW + 30 min. */
    static Appointment pending() {
        return Appointment.request(CUSTOMER_ID, professional(ApprovalMode.MANUAL),
                List.of(service(1L, 60, "50")), START, SALON, null, NOW);
    }

    /** Agendamento CONFIRMED para START. */
    static Appointment confirmed() {
        return Appointment.request(CUSTOMER_ID, professional(ApprovalMode.AUTOMATIC),
                List.of(service(1L, 60, "50")), START, SALON, null, NOW);
    }
}
