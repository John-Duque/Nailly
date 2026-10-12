package com.nailly.infra.persistence.appointment;

import com.nailly.domain.appointment.Appointment;
import com.nailly.domain.appointment.AppointmentGateway;
import com.nailly.domain.appointment.AppointmentItem;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceLocation;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.infra.persistence.professional.JpaProfessionalGateway;
import com.nailly.infra.persistence.salonservice.JpaSalonServiceGateway;
import com.nailly.infra.persistence.salonservice.ProcedureTypeEntity;
import com.nailly.infra.persistence.salonservice.ProcedureTypeJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.nailly.infra.persistence.PersistenceTestData.CUSTOMER_ADDRESS_ID;
import static com.nailly.infra.persistence.PersistenceTestData.CUSTOMER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.LOCATION_ID;
import static com.nailly.infra.persistence.PersistenceTestData.OTHER_USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.newProfessional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaAppointmentGateway.class, JpaProfessionalGateway.class, JpaSalonServiceGateway.class})
class JpaAppointmentGatewayTest {

    private static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
    private static final Instant START = Instant.parse("2026-10-13T13:00:00Z");
    private static final Instant END = START.plusSeconds(90 * 60L);
    private static final ServiceLocation SALON = ServiceLocation.atLocation(LOCATION_ID, "Rua A, 10 - Centro");

    @Autowired
    private AppointmentGateway gateway;
    @Autowired
    private ProfessionalGateway professionalGateway;
    @Autowired
    private SalonServiceGateway salonServiceGateway;
    @Autowired
    private ProcedureTypeJpaRepository procedureTypeRepository;
    @Autowired
    private EntityManager entityManager;

    private Professional automatic;
    private Professional manual;
    private List<SalonService> combo;

    @BeforeEach
    void setUp() {
        automatic = professionalGateway.save(newProfessional(USER_ID, ApprovalMode.AUTOMATIC));
        manual = professionalGateway.save(newProfessional(OTHER_USER_ID, ApprovalMode.MANUAL));
        ProcedureTypeEntity gel = procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true));
        ProcedureType procedureType = new ProcedureType(gel.getId(), gel.getName(), true);
        combo = List.of(
                salonServiceGateway.save(new SalonService(null, automatic.getId(), new ServiceDetails(procedureType,
                        "Gel nas mãos", null, BodyArea.HANDS, 60, Money.of("50"), true))),
                salonServiceGateway.save(new SalonService(null, automatic.getId(), new ServiceDetails(procedureType,
                        "Pés", null, BodyArea.FEET, 30, Money.of("35.50"), true))));
    }

    private Appointment confirmedAtSalon() {
        return gateway.save(Appointment.request(CUSTOMER_ID, automatic, combo, START, SALON, "Primeira vez", NOW));
    }

    private Appointment pendingForManual() {
        SalonService service = salonServiceGateway.save(new SalonService(null, manual.getId(),
                combo.get(0).getDetails()));
        return gateway.save(Appointment.request(CUSTOMER_ID, manual, List.of(service), START, SALON, null, NOW));
    }

    private Appointment reload(long id) {
        entityManager.flush();
        entityManager.clear();
        return gateway.findById(id).orElseThrow();
    }

    @Test
    void savesAndLoadsAppointmentWithItems() {
        Appointment saved = confirmedAtSalon();

        Appointment loaded = reload(saved.getId());

        assertThat(loaded.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(loaded.getProfessionalId()).isEqualTo(automatic.getId());
        assertThat(loaded.getStartAt()).isEqualTo(START);
        assertThat(loaded.getEndAt()).isEqualTo(END);
        assertThat(loaded.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(loaded.getDecidedAt()).isEqualTo(NOW);
        assertThat(loaded.getCreatedAt()).isEqualTo(NOW);
        assertThat(loaded.getExpiresAt()).isNull();
        assertThat(loaded.getLocation().getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(loaded.getLocation().getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(loaded.getLocation().getAddress()).isEqualTo("Rua A, 10 - Centro");
        assertThat(loaded.getItemsTotal()).isEqualTo(Money.of("85.50"));
        assertThat(loaded.getHomeVisitFee()).isEqualTo(Money.ZERO);
        assertThat(loaded.getTotalPrice()).isEqualTo(Money.of("85.50"));
        assertThat(loaded.getNotes()).isEqualTo("Primeira vez");
        assertThat(loaded.getItems())
                .extracting(AppointmentItem::getServiceId, AppointmentItem::getServiceTitle,
                        AppointmentItem::getProcedureTypeName, AppointmentItem::getBodyArea,
                        AppointmentItem::getDurationMinutes, AppointmentItem::getPrice)
                .containsExactly(
                        tuple(combo.get(0).getId(), "Gel nas mãos", "Teste gel", BodyArea.HANDS, 60, Money.of("50")),
                        tuple(combo.get(1).getId(), "Pés", "Teste gel", BodyArea.FEET, 30, Money.of("35.50")));
    }

    @Test
    void savesHomeVisit() {
        ServiceLocation home = ServiceLocation.homeVisit(CUSTOMER_ADDRESS_ID, "Rua B, 20", new BigDecimal("7.2"));
        Appointment saved = gateway.save(Appointment.request(CUSTOMER_ID, automatic, combo, START, home, null, NOW));

        Appointment loaded = reload(saved.getId());

        assertThat(loaded.getLocation().getMode()).isEqualTo(ServiceMode.HOME_VISIT);
        assertThat(loaded.getLocation().getCustomerAddressId()).isEqualTo(CUSTOMER_ADDRESS_ID);
        assertThat(loaded.getLocation().getDistanceKm()).isEqualTo(new BigDecimal("7.2"));
        assertThat(loaded.getHomeVisitFee()).isEqualTo(Money.of("20"));
        assertThat(loaded.getTotalPrice()).isEqualTo(Money.of("105.50"));
    }

    @Test
    void updatesStatusFields() {
        Appointment pending = pendingForManual();
        Instant decidedAt = NOW.plusSeconds(60);
        pending.reject("Agenda cheia", decidedAt);

        gateway.save(pending);

        Appointment loaded = reload(pending.getId());
        assertThat(loaded.getStatus()).isEqualTo(AppointmentStatus.REJECTED);
        assertThat(loaded.getStatusReason()).isEqualTo("Agenda cheia");
        assertThat(loaded.getDecidedAt()).isEqualTo(decidedAt);
        assertThat(loaded.getExpiresAt()).isEqualTo(NOW.plusSeconds(30 * 60L));
        assertThat(loaded.getItems()).hasSize(1);
    }

    @Test
    void savesCancellation() {
        Appointment appointment = confirmedAtSalon();
        appointment.cancel("Imprevisto", NOW.plusSeconds(120));

        gateway.save(appointment);

        Appointment loaded = reload(appointment.getId());
        assertThat(loaded.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(loaded.getCanceledAt()).isEqualTo(NOW.plusSeconds(120));
    }

    @Test
    void confirmedAppointmentOccupiesOverlappingTime() {
        confirmedAtSalon();
        entityManager.flush();
        long professionalId = automatic.getId();

        assertThat(gateway.existsOccupyingSchedule(professionalId, START.minusSeconds(60), START.plusSeconds(60), NOW))
                .isTrue();
        assertThat(gateway.existsOccupyingSchedule(professionalId, END.minusSeconds(60), END.plusSeconds(3600), NOW))
                .isTrue();
        assertThat(gateway.existsOccupyingSchedule(professionalId, START.minusSeconds(3600), START, NOW)).isFalse();
        assertThat(gateway.existsOccupyingSchedule(professionalId, END, END.plusSeconds(3600), NOW)).isFalse();
        assertThat(gateway.existsOccupyingSchedule(manual.getId(), START, END, NOW)).isFalse();
    }

    @Test
    void pendingOccupiesOnlyWhileWithinDeadline() {
        Appointment pending = pendingForManual();
        entityManager.flush();
        Instant deadline = pending.getExpiresAt();

        assertThat(gateway.existsOccupyingSchedule(manual.getId(), START, END, deadline.minusSeconds(1))).isTrue();
        assertThat(gateway.existsOccupyingSchedule(manual.getId(), START, END, deadline)).isFalse();
    }

    @Test
    void canceledAppointmentFreesTime() {
        Appointment appointment = confirmedAtSalon();
        appointment.cancel(null, NOW);
        gateway.save(appointment);
        entityManager.flush();

        assertThat(gateway.existsOccupyingSchedule(automatic.getId(), START, END, NOW)).isFalse();
    }

    @Test
    void findsPendingRequestsPastDeadline() {
        Appointment pending = pendingForManual();
        confirmedAtSalon();
        entityManager.flush();

        assertThat(gateway.findPendingExpiredUntil(pending.getExpiresAt().minusSeconds(1))).isEmpty();
        assertThat(gateway.findPendingExpiredUntil(pending.getExpiresAt()))
                .extracting(Appointment::getId)
                .containsExactly(pending.getId());
    }

    @Test
    void unknownAppointmentIsEmptyAndCannotBeSaved() {
        Appointment ghost = Appointment.builder()
                .id(Long.MAX_VALUE)
                .startAt(START)
                .endAt(END)
                .status(AppointmentStatus.CONFIRMED)
                .location(SALON)
                .build();

        assertThat(gateway.findById(Long.MAX_VALUE)).isEmpty();
        assertThatThrownBy(() -> gateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Agendamento não encontrado.");
    }

    @Test
    void listsAppointmentsTouchingThePeriod() {
        Appointment appointment = confirmedAtSalon();
        appointment.cancel(null, NOW);
        gateway.save(appointment);
        Appointment pending = pendingForManual();
        entityManager.flush();

        assertThat(gateway.findByProfessionalBetween(automatic.getId(), START.minusSeconds(60), START.plusSeconds(60)))
                .extracting(Appointment::getId, Appointment::getStatus)
                .containsExactly(tuple(appointment.getId(), AppointmentStatus.CANCELED));
        assertThat(gateway.findByProfessionalBetween(automatic.getId(), END, END.plusSeconds(3600))).isEmpty();
        assertThat(gateway.findByProfessionalBetween(automatic.getId(), START.minusSeconds(3600), START)).isEmpty();
        assertThat(gateway.findByCustomerBetween(CUSTOMER_ID, START, END))
                .extracting(Appointment::getId)
                .containsExactlyInAnyOrder(appointment.getId(), pending.getId());
        assertThat(gateway.findByCustomerBetween(Long.MAX_VALUE, START, END)).isEmpty();
    }
}
