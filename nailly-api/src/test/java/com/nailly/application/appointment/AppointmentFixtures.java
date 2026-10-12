package com.nailly.application.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.customer.CustomerAddress;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.Place;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
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

    static final long LOCATION_ID = 3L;
    static final long ADDRESS_ID = 4L;

    static Clock clockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    static ProfessionalLocation location() {
        return new ProfessionalLocation(LOCATION_ID, PROFESSIONAL_ID, new Place("Estúdio",
                new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000"), ORIGIN));
    }

    static CustomerAddress customerAddress(GeoPoint location) {
        return new CustomerAddress(ADDRESS_ID, CUSTOMER_ID, new Place("Casa",
                new Address("Rua B", "20", "Apto 3", "Jardins", "São Paulo", "SP", "01400000"), location));
    }

    /** START (13/10/2026 13:00 UTC) é terça às 10:00 em São Paulo. */
    static WorkingHours openTuesday(Long locationId) {
        return new WorkingHours(PROFESSIONAL_ID, locationId,
                List.of(new WorkingPeriod(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))));
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
