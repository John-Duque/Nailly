package com.nailly.infra.persistence.salonservice;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.salonservice.ProcedureType;
import com.nailly.domain.salonservice.ProcedureTypeGateway;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.domain.salonservice.SalonServiceGateway;
import com.nailly.domain.salonservice.ServiceDetails;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.newProfessional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JpaSalonServiceGatewayTest {

    @Autowired
    private SalonServiceGateway gateway;
    @Autowired
    private ProcedureTypeGateway procedureTypeGateway;
    @Autowired
    private ProfessionalGateway professionalGateway;
    @Autowired
    private ProcedureTypeJpaRepository procedureTypeRepository;
    @Autowired
    private EntityManager entityManager;

    private long professionalId;
    private ProcedureType gel;
    private ProcedureType blindagem;

    @BeforeEach
    void setUp() {
        professionalId = professionalGateway.save(newProfessional(USER_ID, ApprovalMode.AUTOMATIC)).getId();
        gel = SalonServiceMapper.toDomain(procedureTypeRepository.save(new ProcedureTypeEntity("Teste gel", true)));
        blindagem = SalonServiceMapper.toDomain(
                procedureTypeRepository.save(new ProcedureTypeEntity("Teste blindagem", true)));
        procedureTypeRepository.save(new ProcedureTypeEntity("Teste inativa", false));
    }

    private SalonService newService(String title) {
        return new SalonService(null, professionalId, new ServiceDetails(gel, title, "Com cutilagem",
                BodyArea.HANDS, 90, Money.of("80.50"), true));
    }

    private SalonService reload(long id) {
        entityManager.flush();
        entityManager.clear();
        return gateway.findById(id).orElseThrow();
    }

    @Test
    void savesAndLoadsAllData() {
        SalonService saved = gateway.save(newService("Gel nas mãos"));

        SalonService loaded = reload(saved.getId());

        assertThat(loaded.getProfessionalId()).isEqualTo(professionalId);
        assertThat(loaded.isActive()).isTrue();
        ServiceDetails details = loaded.getDetails();
        assertThat(details.getProcedureType().getId()).isEqualTo(gel.getId());
        assertThat(details.getProcedureType().getName()).isEqualTo("Teste gel");
        assertThat(details.getTitle()).isEqualTo("Gel nas mãos");
        assertThat(details.getDescription()).isEqualTo("Com cutilagem");
        assertThat(details.getBodyArea()).isEqualTo(BodyArea.HANDS);
        assertThat(details.getDurationMinutes()).isEqualTo(90);
        assertThat(details.getPrice()).isEqualTo(Money.of("80.50"));
        assertThat(details.isAvailableAtHome()).isTrue();
    }

    @Test
    void updatesDetailsAndProcedureType() {
        SalonService service = gateway.save(newService("Gel"));
        service.update(new ServiceDetails(blindagem, "Blindagem nos pés", null, BodyArea.FEET, 60,
                Money.of("70"), false));

        gateway.save(service);

        ServiceDetails details = reload(service.getId()).getDetails();
        assertThat(details.getProcedureType().getName()).isEqualTo("Teste blindagem");
        assertThat(details.getTitle()).isEqualTo("Blindagem nos pés");
        assertThat(details.getDescription()).isNull();
        assertThat(details.getBodyArea()).isEqualTo(BodyArea.FEET);
        assertThat(details.isAvailableAtHome()).isFalse();
    }

    @Test
    void listsOnlyActiveServicesOfProfessionalByTitle() {
        SalonService zeta = gateway.save(newService("Zeta"));
        SalonService alpha = gateway.save(newService("Alpha"));
        SalonService inactive = gateway.save(newService("Inativo"));
        inactive.deactivate();
        gateway.save(inactive);
        entityManager.flush();

        assertThat(gateway.findActiveByProfessional(professionalId))
                .extracting(SalonService::getId)
                .containsExactly(alpha.getId(), zeta.getId());
        assertThat(gateway.findActiveByProfessional(Long.MAX_VALUE)).isEmpty();
        assertThat(reload(inactive.getId()).isActive()).isFalse();
    }

    @Test
    void findsAllByIdsIgnoringUnknown() {
        SalonService first = gateway.save(newService("Primeiro"));
        SalonService second = gateway.save(newService("Segundo"));

        assertThat(gateway.findAllByIds(List.of(first.getId(), second.getId(), Long.MAX_VALUE)))
                .extracting(SalonService::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void unknownServiceIsEmptyAndCannotBeSaved() {
        SalonService ghost = new SalonService(Long.MAX_VALUE, professionalId, newService("X").getDetails());

        assertThat(gateway.findById(Long.MAX_VALUE)).isEmpty();
        assertThatThrownBy(() -> gateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Serviço não encontrado.");
    }

    @Test
    void listsActiveProcedureTypesByName() {
        List<ProcedureType> active = procedureTypeGateway.findAllActive();

        assertThat(active).extracting(ProcedureType::getName)
                .contains("Teste blindagem", "Teste gel")
                .doesNotContain("Teste inativa")
                .isSorted();
        assertThat(procedureTypeGateway.findById(gel.getId())).get()
                .extracting(ProcedureType::getName).isEqualTo("Teste gel");
        assertThat(procedureTypeGateway.findById(Long.MAX_VALUE)).isEmpty();
    }
}
