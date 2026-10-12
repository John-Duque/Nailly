package com.nailly.domain.professional;

import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalTest {

    private static final ApprovalPolicy APPROVAL =
            new ApprovalPolicy(ApprovalMode.AUTOMATIC, ApprovalMode.MANUAL, null);

    private static Professional ana() {
        return new Professional(1L, 7L, "Ana", null, null, APPROVAL, HomeVisitPolicy.notOffered());
    }

    @Test
    void createsActiveProfessionalWithNormalizedTexts() {
        Professional professional = new Professional(1L, 7L, " Ana ", " ", "  Manicure há 10 anos ",
                APPROVAL, HomeVisitPolicy.notOffered());

        assertThat(professional.getId()).isEqualTo(1L);
        assertThat(professional.getUserId()).isEqualTo(7L);
        assertThat(professional.getName()).isEqualTo("Ana");
        assertThat(professional.getPhone()).isNull();
        assertThat(professional.getBio()).isEqualTo("Manicure há 10 anos");
        assertThat(professional.getApprovalPolicy()).isEqualTo(APPROVAL);
        assertThat(professional.getHomeVisitPolicy()).isEqualTo(HomeVisitPolicy.notOffered());
        assertThat(professional.isActive()).isTrue();
    }

    @Test
    void registerUsesDatabaseDefaults() {
        Professional professional = Professional.register(7L, "Ana", "11999990000", null);

        assertThat(professional.getId()).isNull();
        assertThat(professional.getApprovalPolicy()).isEqualTo(ApprovalPolicy.defaults());
        assertThat(professional.getApprovalPolicy().getBookingApproval()).isEqualTo(ApprovalMode.AUTOMATIC);
        assertThat(professional.getApprovalPolicy().getHomeVisitApproval()).isEqualTo(ApprovalMode.MANUAL);
        assertThat(professional.getHomeVisitPolicy().isOffered()).isFalse();
        assertThat(professional.isActive()).isTrue();
    }

    @Test
    void updatesProfile() {
        Professional professional = ana();

        professional.updateProfile(" Ana Paula ", "11999990000", " Nail designer ");

        assertThat(professional.getName()).isEqualTo("Ana Paula");
        assertThat(professional.getPhone()).isEqualTo("11999990000");
        assertThat(professional.getBio()).isEqualTo("Nail designer");
    }

    @Test
    void changesPolicies() {
        Professional professional = ana();
        ApprovalPolicy manual = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 60);
        HomeVisitPolicy homeVisit = new HomeVisitPolicy(true, new GeoPoint(-23.5, -46.6), List.of());

        professional.changeApprovalPolicy(manual);
        professional.changeHomeVisitPolicy(homeVisit);

        assertThat(professional.getApprovalPolicy()).isEqualTo(manual);
        assertThat(professional.getHomeVisitPolicy()).isEqualTo(homeVisit);
    }

    @Test
    void canBeDeactivated() {
        Professional professional = ana();

        professional.deactivate();

        assertThat(professional.isActive()).isFalse();
    }

    @Test
    void requiresName() {
        Professional professional = ana();

        assertThatThrownBy(() -> professional.updateProfile(" ", null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe o nome da profissional.");
    }

    @Test
    void requiresPolicies() {
        Professional professional = ana();

        assertThatThrownBy(() -> professional.changeApprovalPolicy(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe as regras de aprovação.");
        assertThatThrownBy(() -> professional.changeHomeVisitPolicy(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Informe as regras de atendimento a domicílio.");
    }
}
