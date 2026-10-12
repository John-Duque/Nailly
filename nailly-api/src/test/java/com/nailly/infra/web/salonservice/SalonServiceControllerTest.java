package com.nailly.infra.web.salonservice;

import com.nailly.application.salonservice.CreateSalonServiceUseCase;
import com.nailly.application.salonservice.DeactivateSalonServiceUseCase;
import com.nailly.application.salonservice.ListProcedureTypesUseCase;
import com.nailly.application.salonservice.ListProfessionalServicesUseCase;
import com.nailly.application.salonservice.SalonServiceCommand;
import com.nailly.application.salonservice.UpdateSalonServiceUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.salonservice.BodyArea;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static com.nailly.infra.web.WebTestData.GEL;
import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static com.nailly.infra.web.WebTestData.SERVICE_ID;
import static com.nailly.infra.web.WebTestData.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SalonServiceController.class, ProcedureTypeController.class})
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class SalonServiceControllerTest {

    private static final String VALID_SERVICE = """
            {"procedureTypeId": 2, "title": "Gel nas mãos", "description": "Com cutilagem",
             "bodyArea": "HANDS", "durationMinutes": 60, "price": 80.50, "availableAtHome": true}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;

    @MockitoBean
    private CreateSalonServiceUseCase createService;
    @MockitoBean
    private UpdateSalonServiceUseCase updateService;
    @MockitoBean
    private DeactivateSalonServiceUseCase deactivateService;
    @MockitoBean
    private ListProfessionalServicesUseCase listServices;
    @MockitoBean
    private ListProcedureTypesUseCase listProcedureTypes;

    /** Cabeçalho Authorization de uma profissional logada. */
    private String professionalToken(long professionalId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(100L + professionalId, Role.PROFESSIONAL, professionalId, null)).getValue();
    }

    private MockHttpServletRequestBuilder asOwner(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, professionalToken(PROFESSIONAL_ID));
    }

    @Test
    @DisplayName("Técnicas são públicas")
    void procedureTypesArePublic() throws Exception {
        when(listProcedureTypes.execute()).thenReturn(List.of(GEL));

        mockMvc.perform(get("/procedure-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Esmaltação em gel"));
    }

    @Test
    @DisplayName("Catálogo de serviços é público")
    void catalogIsPublic() throws Exception {
        when(listServices.execute(PROFESSIONAL_ID)).thenReturn(List.of(service()));

        mockMvc.perform(get("/professionals/{id}/services", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SERVICE_ID))
                .andExpect(jsonPath("$[0].professionalId").value(PROFESSIONAL_ID))
                .andExpect(jsonPath("$[0].procedureType.name").value("Esmaltação em gel"))
                .andExpect(jsonPath("$[0].title").value("Gel nas mãos"))
                .andExpect(jsonPath("$[0].description").value("Com cutilagem"))
                .andExpect(jsonPath("$[0].bodyArea").value("HANDS"))
                .andExpect(jsonPath("$[0].durationMinutes").value(60))
                .andExpect(jsonPath("$[0].price").value(80.5))
                .andExpect(jsonPath("$[0].availableAtHome").value(true))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("A própria profissional cria serviço")
    void ownerCreatesService() throws Exception {
        when(createService.execute(eq(PROFESSIONAL_ID), any())).thenReturn(service());

        mockMvc.perform(asOwner(post("/professionals/{id}/services", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost/professionals/" + PROFESSIONAL_ID + "/services/" + SERVICE_ID));

        ArgumentCaptor<SalonServiceCommand> captor = ArgumentCaptor.forClass(SalonServiceCommand.class);
        verify(createService).execute(eq(PROFESSIONAL_ID), captor.capture());
        SalonServiceCommand command = captor.getValue();
        assertThat(command.getProcedureTypeId()).isEqualTo(2L);
        assertThat(command.getTitle()).isEqualTo("Gel nas mãos");
        assertThat(command.getDescription()).isEqualTo("Com cutilagem");
        assertThat(command.getBodyArea()).isEqualTo(BodyArea.HANDS);
        assertThat(command.getDurationMinutes()).isEqualTo(60);
        assertThat(command.getPrice()).isEqualTo(Money.of("80.50"));
        assertThat(command.isAvailableAtHome()).isTrue();
    }

    @Test
    @DisplayName("Só a própria profissional altera o catálogo")
    void onlyTheOwnerChangesTheCatalog() throws Exception {
        mockMvc.perform(post("/professionals/{id}/services", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/professionals/{id}/services", PROFESSIONAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isForbidden());
        verifyNoInteractions(createService);
    }

    @Test
    @DisplayName("Recusa serviço inválido listando os campos")
    void invalidServiceIsRefused() throws Exception {
        mockMvc.perform(asOwner(post("/professionals/{id}/services", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"procedureTypeId": 2, "title": "", "bodyArea": "HANDS",
                                 "durationMinutes": 1441, "price": 10.001}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("availableAtHome", "durationMinutes", "price", "title")));
        verifyNoInteractions(createService);
    }

    @Test
    @DisplayName("A própria profissional edita serviço")
    void ownerUpdatesService() throws Exception {
        when(updateService.execute(eq(PROFESSIONAL_ID), eq(SERVICE_ID), any())).thenReturn(service());

        mockMvc.perform(asOwner(put("/professionals/{id}/services/{serviceId}", PROFESSIONAL_ID, SERVICE_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SERVICE_ID));
    }

    @Test
    @DisplayName("Serviço de outra profissional vira 404")
    void serviceOfAnotherProfessionalIs404() throws Exception {
        when(updateService.execute(eq(PROFESSIONAL_ID), eq(99L), any()))
                .thenThrow(new NotFoundException("Serviço não encontrado."));

        mockMvc.perform(asOwner(put("/professionals/{id}/services/99", PROFESSIONAL_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_SERVICE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Serviço não encontrado."));
    }

    @Test
    @DisplayName("A própria profissional desativa serviço")
    void ownerDeactivatesService() throws Exception {
        when(deactivateService.execute(PROFESSIONAL_ID, SERVICE_ID)).thenReturn(service());

        mockMvc.perform(asOwner(post("/professionals/{id}/services/{serviceId}/deactivate",
                        PROFESSIONAL_ID, SERVICE_ID)))
                .andExpect(status().isOk());
        verify(deactivateService).execute(PROFESSIONAL_ID, SERVICE_ID);
    }
}
