package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.shared.Place;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.newProfessional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Locais de atendimento e horários de funcionamento no banco. */
@DataJpaTest
@ActiveProfiles("test")
@Import({JpaProfessionalGateway.class, JpaProfessionalLocationGateway.class, JpaWorkingHoursGateway.class})
class JpaProfessionalPlacesGatewaysTest {

    private static final Address ADDRESS = new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000");
    private static final WorkingPeriod MONDAY_MORNING =
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
    private static final WorkingPeriod MONDAY_AFTERNOON =
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(18, 30));
    private static final WorkingPeriod SATURDAY =
            new WorkingPeriod(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(14, 0));

    @Autowired
    private ProfessionalGateway professionalGateway;
    @Autowired
    private ProfessionalLocationGateway locationGateway;
    @Autowired
    private WorkingHoursGateway workingHoursGateway;
    @Autowired
    private EntityManager entityManager;

    private long professionalId;

    @BeforeEach
    void setUp() {
        professionalId = professionalGateway.save(newProfessional(USER_ID, ApprovalMode.AUTOMATIC)).getId();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private ProfessionalLocation newLocation(String name) {
        return locationGateway.save(new ProfessionalLocation(null, professionalId,
                new Place(name, ADDRESS, new GeoPoint(-23.550520, -46.633308))));
    }

    @Test
    void savesAndListsActiveLocations() {
        ProfessionalLocation studio = newLocation("Estúdio");
        ProfessionalLocation salon = newLocation("Salão");
        salon.deactivate();
        locationGateway.save(salon);
        flushAndClear();

        ProfessionalLocation loaded = locationGateway.findById(studio.getId()).orElseThrow();

        assertThat(loaded.getProfessionalId()).isEqualTo(professionalId);
        assertThat(loaded.getPlace().getName()).isEqualTo("Estúdio");
        assertThat(loaded.getPlace().getAddress()).isEqualTo(ADDRESS);
        assertThat(loaded.getPlace().getLocation()).isEqualTo(new GeoPoint(-23.550520, -46.633308));
        assertThat(locationGateway.findById(salon.getId()).orElseThrow().isActive()).isFalse();
        assertThat(locationGateway.findActiveByProfessional(professionalId))
                .extracting(ProfessionalLocation::getId)
                .containsExactly(studio.getId());
    }

    @Test
    void unknownLocationIsEmptyAndCannotBeSaved() {
        ProfessionalLocation ghost = new ProfessionalLocation(Long.MAX_VALUE, professionalId,
                new Place("Estúdio", ADDRESS, null));

        assertThat(locationGateway.findById(Long.MAX_VALUE)).isEmpty();
        assertThatThrownBy(() -> locationGateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Local não encontrado.");
    }

    @Test
    void savesWorkingHoursPerLocationAndHomeVisit() {
        long locationId = newLocation("Estúdio").getId();

        workingHoursGateway.save(new WorkingHours(professionalId, locationId,
                List.of(MONDAY_AFTERNOON, MONDAY_MORNING)));
        workingHoursGateway.save(new WorkingHours(professionalId, null, List.of(SATURDAY)));
        flushAndClear();

        assertThat(workingHoursGateway.find(professionalId, locationId).getPeriods())
                .containsExactly(MONDAY_MORNING, MONDAY_AFTERNOON);
        assertThat(workingHoursGateway.find(professionalId, null).getPeriods()).containsExactly(SATURDAY);
        assertThat(workingHoursGateway.findAllByProfessional(professionalId))
                .extracting(WorkingHours::getLocationId)
                .containsExactlyInAnyOrder(locationId, null);
    }

    @Test
    void savingReplacesPreviousHoursOfThatPlaceOnly() {
        long locationId = newLocation("Estúdio").getId();
        workingHoursGateway.save(new WorkingHours(professionalId, locationId, List.of(MONDAY_MORNING)));
        workingHoursGateway.save(new WorkingHours(professionalId, null, List.of(SATURDAY)));
        flushAndClear();

        WorkingHours replaced = workingHoursGateway.save(
                new WorkingHours(professionalId, locationId, List.of(MONDAY_AFTERNOON)));
        flushAndClear();

        assertThat(replaced.getPeriods()).containsExactly(MONDAY_AFTERNOON);
        assertThat(workingHoursGateway.find(professionalId, locationId).getPeriods())
                .containsExactly(MONDAY_AFTERNOON);
        assertThat(workingHoursGateway.find(professionalId, null).getPeriods()).containsExactly(SATURDAY);
    }

    @Test
    void placeWithoutHoursIsClosed() {
        assertThat(workingHoursGateway.find(professionalId, null).getPeriods()).isEmpty();
        assertThat(workingHoursGateway.findAllByProfessional(professionalId)).isEmpty();
    }
}
