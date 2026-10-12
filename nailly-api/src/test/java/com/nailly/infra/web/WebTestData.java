package com.nailly.infra.web;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.AppointmentStatus;
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
import java.time.Instant;
import java.util.List;

/** Objetos de domínio devolvidos pelos casos de uso simulados nos testes dos controllers. */
public final class WebTestData {

    public static final long PROFESSIONAL_ID = 10L;
    public static final long SERVICE_ID = 5L;
    public static final long APPOINTMENT_ID = 50L;
    public static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
    public static final Instant START = Instant.parse("2026-10-13T13:00:00Z");
    public static final ProcedureType GEL = new ProcedureType(2L, "Esmaltação em gel", true);

    private WebTestData() {
    }

    public static Professional professional() {
        return new Professional(PROFESSIONAL_ID, 7L, "Ana", "11999990000", "Nail designer",
                new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, 30),
                new HomeVisitPolicy(true, new GeoPoint(-23.55, -46.63),
                        List.of(new HomeVisitFee(new BigDecimal("5"), Money.of("10")))));
    }

    public static SalonService service() {
        return new SalonService(SERVICE_ID, PROFESSIONAL_ID, new ServiceDetails(GEL, "Gel nas mãos",
                "Com cutilagem", BodyArea.HANDS, 60, Money.of("80.50"), true));
    }

    public static Appointment appointment(AppointmentStatus status) {
        return Appointment.builder()
                .id(APPOINTMENT_ID)
                .customerId(20L)
                .professionalId(PROFESSIONAL_ID)
                .startAt(START)
                .endAt(START.plusSeconds(3600))
                .status(status)
                .location(ServiceLocation.atLocation(3L, "Rua A, 10"))
                .items(List.of(new AppointmentItem(SERVICE_ID, "Gel nas mãos", "Esmaltação em gel",
                        BodyArea.HANDS, 60, Money.of("80.50"))))
                .itemsTotal(Money.of("80.50"))
                .totalPrice(Money.of("80.50"))
                .decidedAt(NOW)
                .createdAt(NOW)
                .build();
    }
}
