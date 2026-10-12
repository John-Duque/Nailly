package com.nailly.infra.persistence.professional;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.NotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nailly.infra.persistence.PersistenceTestData.ORIGIN;
import static com.nailly.infra.persistence.PersistenceTestData.OTHER_USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.USER_ID;
import static com.nailly.infra.persistence.PersistenceTestData.fee;
import static com.nailly.infra.persistence.PersistenceTestData.newProfessional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JpaProfessionalGatewayTest {

    @Autowired
    private ProfessionalGateway gateway;
    @Autowired
    private EntityManager entityManager;

    /** Grava no banco e esquece o cache do JPA, para a leitura vir mesmo do banco. */
    private Professional reload(long id) {
        entityManager.flush();
        entityManager.clear();
        return gateway.findById(id).orElseThrow();
    }

    /** Profissional já gravada e lida de novo, como numa requisição de edição. */
    private Professional stored() {
        return reload(gateway.save(newProfessional(USER_ID, ApprovalMode.AUTOMATIC)).getId());
    }

    @Test
    void savesAndLoadsAllData() {
        Professional saved = gateway.save(newProfessional(USER_ID, ApprovalMode.MANUAL));

        Professional loaded = reload(saved.getId());

        assertThat(loaded.getId()).isNotNull();
        assertThat(loaded.getUserId()).isEqualTo(USER_ID);
        assertThat(loaded.getName()).isEqualTo("Ana");
        assertThat(loaded.getPhone()).isEqualTo("11999990000");
        assertThat(loaded.getBio()).isEqualTo("Nail designer");
        assertThat(loaded.getApprovalPolicy())
                .isEqualTo(new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 30));
        assertThat(loaded.getHomeVisitPolicy().isOffered()).isTrue();
        assertThat(loaded.getHomeVisitPolicy().getOrigin()).isEqualTo(ORIGIN);
        assertThat(loaded.getHomeVisitPolicy().getOrigin().getLatitude()).isEqualTo(-23.550520);
        assertThat(loaded.getHomeVisitPolicy().getOrigin().getLongitude()).isEqualTo(-46.633308);
        assertThat(loaded.getHomeVisitPolicy().getFees()).containsExactly(fee("5", "10"), fee("10", "20"));
        assertThat(loaded.isActive()).isTrue();
    }

    @Test
    void updatesFeeBandsKeepingExistingDistances() {
        Professional professional = stored();
        professional.changeHomeVisitPolicy(new HomeVisitPolicy(true, ORIGIN,
                List.of(fee("5", "12"), fee("15", "30"))));

        gateway.save(professional);

        assertThat(reload(professional.getId()).getHomeVisitPolicy().getFees())
                .containsExactly(fee("5", "12"), fee("15", "30"));
    }

    @Test
    void turningHomeVisitOffRemovesOriginAndBands() {
        Professional professional = stored();
        professional.changeHomeVisitPolicy(HomeVisitPolicy.notOffered());

        gateway.save(professional);

        assertThat(reload(professional.getId()).getHomeVisitPolicy()).isEqualTo(HomeVisitPolicy.notOffered());
    }

    @Test
    void updatesProfileAndKeepsDeactivation() {
        Professional professional = stored();
        professional.updateProfile("Ana Paula", null, null);
        professional.deactivate();

        gateway.save(professional);

        Professional loaded = reload(professional.getId());
        assertThat(loaded.getName()).isEqualTo("Ana Paula");
        assertThat(loaded.getPhone()).isNull();
        assertThat(loaded.isActive()).isFalse();
    }

    @Test
    void findsForUpdateAndByUser() {
        Professional saved = gateway.save(newProfessional(USER_ID, ApprovalMode.AUTOMATIC));
        entityManager.flush();

        assertThat(gateway.findByIdForUpdate(saved.getId())).get()
                .extracting(Professional::getUserId).isEqualTo(USER_ID);
        assertThat(gateway.existsByUserId(USER_ID)).isTrue();
        assertThat(gateway.existsByUserId(OTHER_USER_ID)).isFalse();
    }

    @Test
    void unknownIdIsEmpty() {
        assertThat(gateway.findById(Long.MAX_VALUE)).isEmpty();
        assertThat(gateway.findByIdForUpdate(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void savingUnknownIdIsNotFound() {
        Professional ghost = new Professional(Long.MAX_VALUE, USER_ID, "Ana", null, null,
                ApprovalPolicy.defaults(), HomeVisitPolicy.notOffered());

        assertThatThrownBy(() -> gateway.save(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Profissional não encontrada.");
    }
}
