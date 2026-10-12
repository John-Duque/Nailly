package com.nailly.application.professional;

import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalUseCasesTest {

    private static final long PROFESSIONAL_ID = 10L;

    @Mock
    private ProfessionalGateway professionalGateway;

    private static Professional ana() {
        return new Professional(PROFESSIONAL_ID, 7L, "Ana", null, null, ApprovalPolicy.defaults(),
                HomeVisitPolicy.notOffered());
    }

    /** Simula a profissional gravada e usa o getById de verdade da interface. */
    private Professional givenStored() {
        Professional professional = ana();
        when(professionalGateway.getById(PROFESSIONAL_ID)).thenCallRealMethod();
        when(professionalGateway.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));
        return professional;
    }

    private void givenSaveReturnsArgument() {
        when(professionalGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getsProfessional() {
        Professional stored = givenStored();

        assertThat(new GetProfessionalUseCase(professionalGateway).execute(PROFESSIONAL_ID)).isSameAs(stored);
    }

    @Test
    void unknownProfessionalIsNotFound() {
        when(professionalGateway.getById(PROFESSIONAL_ID)).thenCallRealMethod();
        when(professionalGateway.findById(PROFESSIONAL_ID)).thenReturn(Optional.empty());
        GetProfessionalUseCase useCase = new GetProfessionalUseCase(professionalGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Profissional não encontrada.");
    }

    @Test
    void updatesProfile() {
        givenStored();
        givenSaveReturnsArgument();

        Professional professional = new UpdateProfessionalProfileUseCase(professionalGateway)
                .execute(PROFESSIONAL_ID, "Ana Paula", "11999990000", "Nail designer");

        assertThat(professional.getName()).isEqualTo("Ana Paula");
        assertThat(professional.getPhone()).isEqualTo("11999990000");
        assertThat(professional.getBio()).isEqualTo("Nail designer");
    }

    @Test
    void updatesApprovalPolicy() {
        givenStored();
        givenSaveReturnsArgument();
        ApprovalPolicy manual = new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 60);

        Professional professional = new UpdateApprovalPolicyUseCase(professionalGateway)
                .execute(PROFESSIONAL_ID, manual);

        assertThat(professional.getApprovalPolicy()).isEqualTo(manual);
    }

    @Test
    void updatesHomeVisitPolicy() {
        givenStored();
        givenSaveReturnsArgument();
        HomeVisitPolicy homeVisit = new HomeVisitPolicy(true, new GeoPoint(-23.5, -46.6), List.of());

        Professional professional = new UpdateHomeVisitPolicyUseCase(professionalGateway)
                .execute(PROFESSIONAL_ID, homeVisit);

        assertThat(professional.getHomeVisitPolicy()).isEqualTo(homeVisit);
    }

    @Test
    void deactivatesProfessional() {
        givenStored();
        givenSaveReturnsArgument();

        Professional professional = new DeactivateProfessionalUseCase(professionalGateway).execute(PROFESSIONAL_ID);

        assertThat(professional.isActive()).isFalse();
    }

    @Test
    void invalidChangeIsNotSaved() {
        givenStored();
        UpdateProfessionalProfileUseCase useCase = new UpdateProfessionalProfileUseCase(professionalGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, " ", null, null))
                .isInstanceOf(DomainException.class);
        verify(professionalGateway, never()).save(any());
    }
}
