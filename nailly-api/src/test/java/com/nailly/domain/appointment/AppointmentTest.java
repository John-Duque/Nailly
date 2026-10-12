package com.nailly.domain.appointment;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class AppointmentTest {

    private static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
    private static final Instant START = Instant.parse("2026-10-13T13:00:00Z");
    private static final long CUSTOMER_ID = 20L;
    private static final long PROFESSIONAL_ID = 10L;
    private static final ProcedureType GEL = new ProcedureType(2L, "Esmaltação em gel", true);

    private static final ApprovalPolicy AUTOMATIC =
            new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.AUTOMATIC, null);
    private static final ApprovalPolicy MANUAL_30_MIN =
            new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30);
    private static final HomeVisitPolicy HOME_VISIT = new HomeVisitPolicy(true, new GeoPoint(-23.55, -46.63),
            List.of(new HomeVisitFee(new BigDecimal("5"), Money.of("10")),
                    new HomeVisitFee(new BigDecimal("10"), Money.of("20"))));

    private static final ServiceLocation SALON = ServiceLocation.atLocation(3L, "Rua A, 10");

    private static Professional professional(ApprovalPolicy approval, HomeVisitPolicy homeVisit) {
        return new Professional(PROFESSIONAL_ID, 1L, "Ana", null, null, approval, homeVisit);
    }

    private static SalonService service(long id, int minutes, String price, boolean availableAtHome) {
        return new SalonService(id, PROFESSIONAL_ID, new ServiceDetails(GEL, "Serviço " + id, null,
                BodyArea.HANDS, minutes, Money.of(price), availableAtHome));
    }

    private static ServiceLocation homeAt(String distanceKm) {
        return ServiceLocation.homeVisit(4L, "Rua B, 20", new BigDecimal(distanceKm));
    }

    private static Appointment request(ApprovalPolicy approval, ServiceLocation location) {
        return Appointment.request(CUSTOMER_ID, professional(approval, HOME_VISIT),
                List.of(service(1L, 60, "50", true)), START, location, null, NOW);
    }

    private static Appointment pending() {
        return request(MANUAL_30_MIN, SALON);
    }

    private static Appointment confirmed() {
        return request(AUTOMATIC, SALON);
    }

    @Nested
    class Request {

        @Test
        void comboAddsDurationsAndPricesAndCopiesServices() {
            Appointment appointment = Appointment.request(CUSTOMER_ID, professional(AUTOMATIC, HOME_VISIT),
                    List.of(service(1L, 60, "50", true), service(2L, 45, "35.50", true)),
                    START, SALON, "  Primeira vez  ", NOW);

            assertThat(appointment.getId()).isNull();
            assertThat(appointment.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(appointment.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
            assertThat(appointment.getStartAt()).isEqualTo(START);
            assertThat(appointment.getEndAt()).isEqualTo(Instant.parse("2026-10-13T14:45:00Z"));
            assertThat(appointment.getItemsTotal()).isEqualTo(Money.of("85.50"));
            assertThat(appointment.getHomeVisitFee()).isEqualTo(Money.ZERO);
            assertThat(appointment.getTotalPrice()).isEqualTo(Money.of("85.50"));
            assertThat(appointment.getLocation()).isEqualTo(SALON);
            assertThat(appointment.getNotes()).isEqualTo("Primeira vez");
            assertThat(appointment.getCreatedAt()).isEqualTo(NOW);
            assertThat(appointment.getItems())
                    .extracting(AppointmentItem::getServiceId, AppointmentItem::getServiceTitle,
                            AppointmentItem::getProcedureTypeName, AppointmentItem::getBodyArea,
                            AppointmentItem::getDurationMinutes, AppointmentItem::getPrice)
                    .containsExactly(
                            tuple(1L, "Serviço 1", "Esmaltação em gel", BodyArea.HANDS, 60, Money.of("50")),
                            tuple(2L, "Serviço 2", "Esmaltação em gel", BodyArea.HANDS, 45, Money.of("35.50")));
        }

        @Test
        void automaticApprovalIsBornConfirmed() {
            Appointment appointment = confirmed();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(appointment.getDecidedAt()).isEqualTo(NOW);
            assertThat(appointment.getExpiresAt()).isNull();
        }

        @Test
        void manualApprovalIsBornPendingWithDeadline() {
            Appointment appointment = pending();

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
            assertThat(appointment.getDecidedAt()).isNull();
            assertThat(appointment.getExpiresAt()).isEqualTo(Instant.parse("2026-10-12T12:30:00Z"));
        }

        @Test
        void manualApprovalWithoutDeadlineNeverExpires() {
            ApprovalPolicy noDeadline = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, null);

            Appointment appointment = request(noDeadline, SALON);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
            assertThat(appointment.getExpiresAt()).isNull();
        }

        @Test
        void deadlineNeverPassesStartTime() {
            Instant soon = NOW.plusSeconds(600);

            Appointment appointment = Appointment.request(CUSTOMER_ID, professional(MANUAL_30_MIN, HOME_VISIT),
                    List.of(service(1L, 60, "50", true)), soon, SALON, null, NOW);

            assertThat(appointment.getExpiresAt()).isEqualTo(soon);
        }

        @Test
        void eachModeUsesItsOwnApproval() {
            ApprovalPolicy mixed = new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, null);

            assertThat(request(mixed, SALON).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(request(mixed, homeAt("3")).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @Test
        void homeVisitChargesSmallestBandThatCoversDistance() {
            assertThat(request(AUTOMATIC, homeAt("5.0")).getHomeVisitFee()).isEqualTo(Money.of("10"));

            Appointment appointment = request(AUTOMATIC, homeAt("7.2"));

            assertThat(appointment.getHomeVisitFee()).isEqualTo(Money.of("20"));
            assertThat(appointment.getTotalPrice()).isEqualTo(Money.of("70"));
        }

        @Test
        void homeVisitBeyondLastBandIsRefused() {
            ServiceLocation farAway = homeAt("10.1");

            assertThatThrownBy(() -> request(AUTOMATIC, farAway))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O endereço está fora da área de atendimento a domicílio.");
        }

        @Test
        void homeVisitRequiresProfessionalToOfferIt() {
            Professional professional = professional(AUTOMATIC, HomeVisitPolicy.notOffered());
            List<SalonService> services = List.of(service(1L, 60, "50", true));
            ServiceLocation home = homeAt("1");

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, home, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Esta profissional não atende a domicílio.");
        }

        @Test
        void homeVisitRequiresEveryServiceAvailableAtHome() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            List<SalonService> services = List.of(service(1L, 60, "50", true), service(2L, 30, "20", false));
            ServiceLocation home = homeAt("1");

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, home, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O serviço Serviço 2 não é feito a domicílio.");
        }

        @Test
        void serviceNotAvailableAtHomeCanBeBookedAtLocation() {
            Appointment appointment = Appointment.request(CUSTOMER_ID, professional(AUTOMATIC, HOME_VISIT),
                    List.of(service(1L, 60, "50", false)), START, SALON, null, NOW);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        void requiresAtLeastOneService() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            List<SalonService> none = List.of();

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, none, START, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Escolha ao menos um serviço.");
        }

        @Test
        void requiresServicesFromSameProfessional() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            SalonService otherProfessionals = new SalonService(9L, 99L,
                    new ServiceDetails(GEL, "Outro", null, BodyArea.FEET, 30, Money.of("20"), true));
            List<SalonService> services = List.of(service(1L, 60, "50", true), otherProfessionals);

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Todos os serviços devem ser da mesma profissional.");
        }

        @Test
        void rejectsInactiveService() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            SalonService inactive = service(1L, 60, "50", true);
            inactive.deactivate();
            List<SalonService> services = List.of(inactive);

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O serviço Serviço 1 não está disponível.");
        }

        @Test
        void rejectsRepeatedService() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            List<SalonService> services = List.of(service(1L, 60, "50", true), service(1L, 60, "50", true));

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O serviço Serviço 1 foi escolhido mais de uma vez.");
        }

        @Test
        void rejectsInactiveProfessional() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            professional.deactivate();
            List<SalonService> services = List.of(service(1L, 60, "50", true));

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, START, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Esta profissional não está recebendo agendamentos.");
        }

        @Test
        void rejectsStartTimeNotInFuture() {
            Professional professional = professional(AUTOMATIC, HOME_VISIT);
            List<SalonService> services = List.of(service(1L, 60, "50", true));

            assertThatThrownBy(() -> Appointment.request(CUSTOMER_ID, professional, services, NOW, SALON, null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Escolha um horário no futuro.");
        }
    }

    @Nested
    class Decision {

        @Test
        void confirmsPendingRequest() {
            Appointment appointment = pending();
            Instant decidedAt = NOW.plusSeconds(60);

            appointment.confirm(decidedAt);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(appointment.getDecidedAt()).isEqualTo(decidedAt);
        }

        @Test
        void cannotConfirmAfterDeadline() {
            Appointment appointment = pending();
            Instant deadline = appointment.getExpiresAt();

            assertThatThrownBy(() -> appointment.confirm(deadline))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O prazo para responder este pedido acabou.");
        }

        @Test
        void cannotConfirmTwice() {
            Appointment appointment = confirmed();

            assertThatThrownBy(() -> appointment.confirm(NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Só é possível confirmar um pedido pendente.");
        }

        @Test
        void rejectsPendingRequestWithReason() {
            Appointment appointment = pending();

            appointment.reject(" Agenda cheia ", NOW);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.REJECTED);
            assertThat(appointment.getStatusReason()).isEqualTo("Agenda cheia");
            assertThat(appointment.getDecidedAt()).isEqualTo(NOW);
        }

        @Test
        void cannotRejectConfirmedAppointment() {
            Appointment appointment = confirmed();

            assertThatThrownBy(() -> appointment.reject(null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Só é possível recusar um pedido pendente.");
        }

        @Test
        void expiresWhenDeadlinePasses() {
            Appointment appointment = pending();
            Instant deadline = appointment.getExpiresAt();

            assertThat(appointment.isExpiredAt(deadline.minusSeconds(1))).isFalse();
            assertThat(appointment.isExpiredAt(deadline)).isTrue();

            appointment.expire(deadline);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.EXPIRED);
            assertThat(appointment.isExpiredAt(deadline)).isFalse();
        }

        @Test
        void cannotExpireBeforeDeadline() {
            Appointment appointment = pending();

            assertThatThrownBy(() -> appointment.expire(NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O pedido ainda está dentro do prazo.");
        }

        @Test
        void requestWithoutDeadlineNeverExpires() {
            Appointment appointment = request(new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, null), SALON);
            Instant later = START.plusSeconds(86_400);

            assertThat(appointment.isExpiredAt(later)).isFalse();
            assertThatThrownBy(() -> appointment.expire(later)).isInstanceOf(DomainException.class);
        }

        @Test
        void cannotExpireConfirmedAppointment() {
            Appointment appointment = confirmed();

            assertThatThrownBy(() -> appointment.expire(START))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Só um pedido pendente pode expirar.");
        }
    }

    @Nested
    class Cancellation {

        @Test
        void cancelsBeforeStartTime() {
            Appointment appointment = confirmed();
            Instant canceledAt = START.minusSeconds(1);

            appointment.cancel("Imprevisto", canceledAt);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
            assertThat(appointment.getStatusReason()).isEqualTo("Imprevisto");
            assertThat(appointment.getCanceledAt()).isEqualTo(canceledAt);
        }

        @Test
        void pendingRequestCanBeCanceled() {
            Appointment appointment = pending();

            appointment.cancel(null, NOW);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        }

        @Test
        void cannotCancelAfterStartTime() {
            Appointment appointment = confirmed();

            assertThatThrownBy(() -> appointment.cancel(null, START))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Não é possível cancelar depois do horário marcado.");
        }

        @Test
        void cannotCancelFinishedAppointment() {
            Appointment appointment = confirmed();
            appointment.complete(START);

            assertThatThrownBy(() -> appointment.cancel(null, NOW))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Este agendamento não pode mais ser cancelado.");
        }
    }

    @Nested
    class Attendance {

        @Test
        void completesAfterStartTime() {
            Appointment appointment = confirmed();

            appointment.complete(START);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        void marksNoShowAfterStartTime() {
            Appointment appointment = confirmed();

            appointment.markNoShow(START.plusSeconds(900));

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        }

        @Test
        void cannotFinishBeforeStartTime() {
            Appointment appointment = confirmed();
            Instant beforeStart = START.minusSeconds(1);

            assertThatThrownBy(() -> appointment.complete(beforeStart))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("O atendimento ainda não começou.");
        }

        @Test
        void cannotFinishPendingRequest() {
            Appointment appointment = pending();

            assertThatThrownBy(() -> appointment.markNoShow(START))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Só é possível finalizar um agendamento confirmado.");
        }
    }

    @Nested
    class Schedule {

        @Test
        void conflictsOnlyWhenIntervalsOverlap() {
            Appointment appointment = confirmed();
            Instant end = appointment.getEndAt();

            assertThat(appointment.conflictsWith(START.minusSeconds(1800), START.plusSeconds(1))).isTrue();
            assertThat(appointment.conflictsWith(end.minusSeconds(1), end.plusSeconds(3600))).isTrue();
            assertThat(appointment.conflictsWith(START.minusSeconds(3600), START)).isFalse();
            assertThat(appointment.conflictsWith(end, end.plusSeconds(3600))).isFalse();
        }

        @Test
        void onlyPendingAndConfirmedOccupySchedule() {
            assertThat(AppointmentStatus.values())
                    .filteredOn(AppointmentStatus::occupiesSchedule)
                    .containsExactlyInAnyOrder(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

            Appointment canceled = confirmed();
            canceled.cancel(null, NOW);

            assertThat(canceled.conflictsWith(START, START.plusSeconds(60))).isFalse();
        }
    }

    @Test
    void builderRestoresStoredData() {
        Appointment appointment = Appointment.builder()
                .id(50L)
                .customerId(CUSTOMER_ID)
                .professionalId(PROFESSIONAL_ID)
                .startAt(START)
                .endAt(START.plusSeconds(3600))
                .status(AppointmentStatus.CANCELED)
                .expiresAt(NOW.plusSeconds(60))
                .location(SALON)
                .items(List.of(new AppointmentItem(1L, "Gel", "Esmaltação em gel", BodyArea.HANDS, 60,
                        Money.of("50"))))
                .itemsTotal(Money.of("50"))
                .totalPrice(Money.of("50"))
                .notes("obs")
                .statusReason("Imprevisto")
                .decidedAt(NOW)
                .canceledAt(NOW.plusSeconds(30))
                .createdAt(NOW)
                .build();

        assertThat(appointment.getId()).isEqualTo(50L);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appointment.getExpiresAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(appointment.getStatusReason()).isEqualTo("Imprevisto");
        assertThat(appointment.getDecidedAt()).isEqualTo(NOW);
        assertThat(appointment.getCanceledAt()).isEqualTo(NOW.plusSeconds(30));
        assertThat(appointment.getHomeVisitFee()).isEqualTo(Money.ZERO);
        assertThat(appointment.getItems()).singleElement()
                .extracting(AppointmentItem::getServiceTitle).isEqualTo("Gel");
    }

    @Test
    void builderRequiresMainFields() {
        Appointment.Builder withoutLocation = Appointment.builder()
                .startAt(START).endAt(START.plusSeconds(60)).status(AppointmentStatus.PENDING);

        assertThatThrownBy(withoutLocation::build).isInstanceOf(NullPointerException.class);
    }
}
