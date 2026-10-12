package com.nailly.infra.web.professional;

import com.nailly.application.professional.AddProfessionalLocationUseCase;
import com.nailly.application.professional.DeactivateProfessionalLocationUseCase;
import com.nailly.application.professional.DefineWorkingHoursUseCase;
import com.nailly.application.professional.ListProfessionalLocationsUseCase;
import com.nailly.application.professional.ListWorkingHoursUseCase;
import com.nailly.application.professional.UpdateProfessionalLocationUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.professional.ProfessionalLocation;
import com.nailly.domain.professional.WorkingHours;
import com.nailly.domain.professional.WorkingPeriod;
import com.nailly.domain.shared.Address;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.Place;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Locais de atendimento e horário de funcionamento: consulta pública, alteração só da dona. */
@WebMvcTest({ProfessionalLocationController.class, WorkingHoursController.class})
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class ProfessionalPlacesControllersTest {

    private static final long LOCATION_ID = 3L;
    private static final String STUDIO_JSON = """
            {"name": "Estúdio", "street": "Rua A", "number": "10", "neighborhood": "Centro",
             "city": "São Paulo", "state": "SP", "zipCode": "01000-000"}
            """;
    private static final List<WorkingPeriod> MONDAY = List.of(
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
            new WorkingPeriod(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(18, 0)));

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;

    @MockitoBean
    private AddProfessionalLocationUseCase addLocation;
    @MockitoBean
    private UpdateProfessionalLocationUseCase updateLocation;
    @MockitoBean
    private DeactivateProfessionalLocationUseCase deactivateLocation;
    @MockitoBean
    private ListProfessionalLocationsUseCase listLocations;
    @MockitoBean
    private DefineWorkingHoursUseCase defineWorkingHours;
    @MockitoBean
    private ListWorkingHoursUseCase listWorkingHours;

    /** Cabeçalho Authorization de uma profissional logada. */
    private String professionalToken(long professionalId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(100L + professionalId, Role.PROFESSIONAL, professionalId, null)).getValue();
    }

    private MockHttpServletRequestBuilder asOwner(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, professionalToken(PROFESSIONAL_ID));
    }

    private static ProfessionalLocation studio() {
        return new ProfessionalLocation(LOCATION_ID, PROFESSIONAL_ID, new Place("Estúdio",
                new Address("Rua A", "10", null, "Centro", "São Paulo", "SP", "01000-000"), null));
    }

    @Test
    void locationsArePublic() throws Exception {
        when(listLocations.execute(PROFESSIONAL_ID)).thenReturn(List.of(studio()));

        mockMvc.perform(get("/professionals/{id}/locations", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(LOCATION_ID))
                .andExpect(jsonPath("$[0].name").value("Estúdio"))
                .andExpect(jsonPath("$[0].latitude").doesNotExist());
    }

    @Test
    void ownerManagesLocations() throws Exception {
        ProfessionalLocation closed = studio();
        closed.deactivate();
        when(addLocation.execute(eq(PROFESSIONAL_ID), any())).thenReturn(studio());
        when(updateLocation.execute(eq(PROFESSIONAL_ID), eq(LOCATION_ID), any())).thenReturn(studio());
        when(deactivateLocation.execute(PROFESSIONAL_ID, LOCATION_ID)).thenReturn(closed);

        mockMvc.perform(asOwner(post("/professionals/{id}/locations", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDIO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formatted").value("Rua A, 10, Centro, São Paulo/SP, 01000-000"));
        mockMvc.perform(asOwner(put("/professionals/{id}/locations/{locationId}", PROFESSIONAL_ID, LOCATION_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDIO_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(asOwner(post("/professionals/{id}/locations/{locationId}/deactivate",
                        PROFESSIONAL_ID, LOCATION_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void anotherProfessionalCannotChangeLocations() throws Exception {
        mockMvc.perform(post("/professionals/{id}/locations", PROFESSIONAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDIO_JSON))
                .andExpect(status().isForbidden());
        verifyNoInteractions(addLocation);
    }

    @Test
    void workingHoursArePublic() throws Exception {
        when(listWorkingHours.execute(PROFESSIONAL_ID))
                .thenReturn(List.of(new WorkingHours(PROFESSIONAL_ID, LOCATION_ID, MONDAY),
                        new WorkingHours(PROFESSIONAL_ID, null, MONDAY.subList(0, 1))));

        mockMvc.perform(get("/professionals/{id}/working-hours", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationId").value(LOCATION_ID))
                .andExpect(jsonPath("$[0].homeVisit").value(false))
                .andExpect(jsonPath("$[0].periods[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].periods[0].start").value("09:00:00"))
                .andExpect(jsonPath("$[0].periods[1].end").value("18:00:00"))
                .andExpect(jsonPath("$[1].homeVisit").value(true));
    }

    @Test
    void ownerDefinesWorkingHours() throws Exception {
        when(defineWorkingHours.execute(PROFESSIONAL_ID, LOCATION_ID, MONDAY))
                .thenReturn(new WorkingHours(PROFESSIONAL_ID, LOCATION_ID, MONDAY));

        mockMvc.perform(asOwner(put("/professionals/{id}/working-hours", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"locationId": 3, "periods": [
                                  {"dayOfWeek": "MONDAY", "start": "09:00", "end": "12:00"},
                                  {"dayOfWeek": "MONDAY", "start": "13:00", "end": "18:00"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periods.length()").value(2));
        verify(defineWorkingHours).execute(PROFESSIONAL_ID, LOCATION_ID, MONDAY);
    }

    @Test
    void invalidWorkingHoursAreRefused() throws Exception {
        mockMvc.perform(asOwner(put("/professionals/{id}/working-hours", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"periods": [{"dayOfWeek": "MONDAY", "start": "09:00"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("periods[0].end")));
        mockMvc.perform(asOwner(put("/professionals/{id}/working-hours", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("periods"));
        verifyNoInteractions(defineWorkingHours);
    }

    @Test
    void overlappingHoursBecome422() throws Exception {
        when(defineWorkingHours.execute(eq(PROFESSIONAL_ID), isNull(), any()))
                .thenThrow(new DomainException("Há horários sobrepostos no mesmo dia."));

        mockMvc.perform(asOwner(put("/professionals/{id}/working-hours", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"periods": [{"dayOfWeek": "MONDAY", "start": "09:00", "end": "12:00"}]}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Há horários sobrepostos no mesmo dia."));
    }
}
