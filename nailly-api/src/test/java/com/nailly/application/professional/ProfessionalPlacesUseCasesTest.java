package com.nailly.application.professional;

import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.ProfessionalLocationGateway;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingHoursGateway;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.shared.Place;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Locais de atendimento e horário de funcionamento. */
@ExtendWith(MockitoExtension.class)
class ProfessionalPlacesUseCasesTest {

    private static final long PROFESSIONAL_ID = 10L;
    private static final long LOCATION_ID = 3L;
    private static final Place STUDIO = new Place("Estúdio",
            new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000"), null);
    private static final List<WorkingPeriod> MONDAY = List.of(
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(18, 0)));

    @Mock
    private ProfessionalGateway professionalGateway;
    @Mock
    private ProfessionalLocationGateway locationGateway;
    @Mock
    private WorkingHoursGateway workingHoursGateway;

    private void givenProfessional() {
        when(professionalGateway.getById(PROFESSIONAL_ID)).thenReturn(new Professional(PROFESSIONAL_ID, 7L, "Ana",
                null, null, ApprovalPolicy.defaults(), HomeVisitPolicy.notOffered()));
    }

    private ProfessionalLocation givenLocation(long ownerId) {
        ProfessionalLocation location = new ProfessionalLocation(LOCATION_ID, ownerId, STUDIO);
        when(locationGateway.getByIdAndProfessional(anyLong(), anyLong())).thenCallRealMethod();
        when(locationGateway.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        return location;
    }

    private void givenLocationSaveReturnsArgument() {
        when(locationGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private DefineWorkingHoursUseCase defineWorkingHours() {
        return new DefineWorkingHoursUseCase(professionalGateway, locationGateway, workingHoursGateway);
    }

    @Test
    void addsLocation() {
        givenProfessional();
        givenLocationSaveReturnsArgument();

        ProfessionalLocation location = new AddProfessionalLocationUseCase(professionalGateway, locationGateway)
                .execute(PROFESSIONAL_ID, STUDIO);

        assertThat(location.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(location.getPlace()).isSameAs(STUDIO);
    }

    @Test
    void updatesOwnLocation() {
        givenLocation(PROFESSIONAL_ID);
        givenLocationSaveReturnsArgument();
        Place renamed = new Place("Salão", STUDIO.getAddress(), null);

        ProfessionalLocation location = new UpdateProfessionalLocationUseCase(locationGateway)
                .execute(PROFESSIONAL_ID, LOCATION_ID, renamed);

        assertThat(location.getPlace().getName()).isEqualTo("Salão");
    }

    @Test
    void locationOfAnotherProfessionalIsNotFound() {
        givenLocation(99L);
        DeactivateProfessionalLocationUseCase useCase = new DeactivateProfessionalLocationUseCase(locationGateway);

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, LOCATION_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Local não encontrado.");
        verify(locationGateway, never()).save(any());
    }

    @Test
    void deactivatesOwnLocation() {
        givenLocation(PROFESSIONAL_ID);
        givenLocationSaveReturnsArgument();

        assertThat(new DeactivateProfessionalLocationUseCase(locationGateway)
                .execute(PROFESSIONAL_ID, LOCATION_ID).isActive()).isFalse();
    }

    @Test
    void listsActiveLocations() {
        List<ProfessionalLocation> locations = List.of(new ProfessionalLocation(LOCATION_ID, PROFESSIONAL_ID, STUDIO));
        when(locationGateway.findActiveByProfessional(PROFESSIONAL_ID)).thenReturn(locations);

        assertThat(new ListProfessionalLocationsUseCase(locationGateway).execute(PROFESSIONAL_ID))
                .isEqualTo(locations);
    }

    @Test
    void definesWorkingHoursOfOwnLocation() {
        givenProfessional();
        givenLocation(PROFESSIONAL_ID);
        when(workingHoursGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkingHours hours = defineWorkingHours().execute(PROFESSIONAL_ID, LOCATION_ID, MONDAY);

        assertThat(hours.getLocationId()).isEqualTo(LOCATION_ID);
        assertThat(hours.getPeriods()).containsExactlyElementsOf(MONDAY);
    }

    @Test
    void definesHomeVisitHoursWithoutLocation() {
        givenProfessional();
        when(workingHoursGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkingHours hours = defineWorkingHours().execute(PROFESSIONAL_ID, null, MONDAY);

        assertThat(hours.isHomeVisit()).isTrue();
        verifyNoInteractions(locationGateway);
    }

    @Test
    void refusesHoursForInactiveLocation() {
        givenProfessional();
        givenLocation(PROFESSIONAL_ID).deactivate();
        DefineWorkingHoursUseCase useCase = defineWorkingHours();

        assertThatThrownBy(() -> useCase.execute(PROFESSIONAL_ID, LOCATION_ID, MONDAY))
                .isInstanceOf(DomainException.class)
                .hasMessage("Este local está desativado.");
        verify(workingHoursGateway, never()).save(any());
    }

    @Test
    void listsWorkingHours() {
        List<WorkingHours> all = List.of(new WorkingHours(PROFESSIONAL_ID, null, MONDAY));
        when(workingHoursGateway.findAllByProfessional(PROFESSIONAL_ID)).thenReturn(all);

        assertThat(new ListWorkingHoursUseCase(workingHoursGateway).execute(PROFESSIONAL_ID)).isEqualTo(all);
    }
}
