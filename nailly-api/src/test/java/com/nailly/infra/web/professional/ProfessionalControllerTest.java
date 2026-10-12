package com.nailly.infra.web.professional;

import com.nailly.application.appointment.ListProfessionalAgendaUseCase;
import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static com.nailly.infra.web.WebTestData.appointment;
import static com.nailly.infra.web.WebTestData.professional;
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

@WebMvcTest(ProfessionalController.class)
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class ProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;

    @MockitoBean
    private GetProfessionalUseCase getProfessional;
    @MockitoBean
    private UpdateProfessionalProfileUseCase updateProfile;
    @MockitoBean
    private UpdateApprovalPolicyUseCase updateApprovalPolicy;
    @MockitoBean
    private UpdateHomeVisitPolicyUseCase updateHomeVisitPolicy;
    @MockitoBean
    private DeactivateProfessionalUseCase deactivateProfessional;
    @MockitoBean
    private ListProfessionalAgendaUseCase listAgenda;

    /** Cabeçalho Authorization de uma profissional logada. */
    private String professionalToken(long professionalId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(100L + professionalId, Role.PROFESSIONAL, professionalId, null)).getValue();
    }

    /** Cabeçalho Authorization de uma cliente logada. */
    private String customerToken(long customerId) {
        return "Bearer " + tokens.issue(
                new AuthenticatedAccount(200L + customerId, Role.CUSTOMER, null, customerId)).getValue();
    }

    /** Requisição feita pela própria profissional. */
    private MockHttpServletRequestBuilder asOwner(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, professionalToken(PROFESSIONAL_ID));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void profileIsPublic() throws Exception {
        when(getProfessional.execute(PROFESSIONAL_ID)).thenReturn(professional());

        mockMvc.perform(get("/professionals/{id}", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROFESSIONAL_ID))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.bio").value("Nail designer"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.approvalPolicy.bookingApproval").value("AUTOMATIC"))
                .andExpect(jsonPath("$.approvalPolicy.requestExpirationMinutes").value(30))
                .andExpect(jsonPath("$.homeVisitPolicy.offered").value(true))
                .andExpect(jsonPath("$.homeVisitPolicy.originLatitude").value(-23.55))
                .andExpect(jsonPath("$.homeVisitPolicy.fees[0].maxDistanceKm").value(5.0))
                .andExpect(jsonPath("$.homeVisitPolicy.fees[0].fee").value(10.0));
    }

    @Test
    void unknownProfessionalBecomes404() throws Exception {
        when(getProfessional.execute(99L)).thenThrow(new NotFoundException("Profissional não encontrada."));

        mockMvc.perform(get("/professionals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Não encontrado"))
                .andExpect(jsonPath("$.detail").value("Profissional não encontrada."))
                .andExpect(jsonPath("$.instance").value("/professionals/99"));
    }

    @Test
    void nonNumericIdBecomes400() throws Exception {
        mockMvc.perform(get("/professionals/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O valor 'abc' não é válido para 'id'."));
    }

    @Test
    void ownerUpdatesProfile() throws Exception {
        when(updateProfile.execute(PROFESSIONAL_ID, "Ana Paula", null, "Bio")).thenReturn(professional());

        mockMvc.perform(asOwner(json(put("/professionals/{id}/profile", PROFESSIONAL_ID), """
                        {"name": "Ana Paula", "bio": "Bio"}
                        """)))
                .andExpect(status().isOk());
        verify(updateProfile).execute(PROFESSIONAL_ID, "Ana Paula", null, "Bio");
    }

    @Test
    void changesRequireLogin() throws Exception {
        mockMvc.perform(json(put("/professionals/{id}/profile", PROFESSIONAL_ID), "{\"name\": \"Ana\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("Não autenticado"));
        mockMvc.perform(put("/professionals/{id}/profile", PROFESSIONAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(updateProfile);
    }

    @Test
    void anotherProfessionalCannotChangeData() throws Exception {
        mockMvc.perform(json(post("/professionals/{id}/deactivate", PROFESSIONAL_ID), "{}")
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"))
                .andExpect(jsonPath("$.detail").value("Você só pode alterar os seus próprios dados."));
        mockMvc.perform(post("/professionals/{id}/deactivate", PROFESSIONAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, customerToken(20L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso permitido apenas para profissionais."));
        verifyNoInteractions(deactivateProfessional);
    }

    @Test
    void invalidBodyListsEveryField() throws Exception {
        mockMvc.perform(asOwner(json(put("/professionals/{id}/profile", PROFESSIONAL_ID), """
                        {"name": " ", "phone": "123456789012345678901"}
                        """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Informe o nome."))
                .andExpect(jsonPath("$.errors[1].field").value("phone"));
        verifyNoInteractions(updateProfile);
    }

    @Test
    void businessRuleBecomes422() throws Exception {
        when(updateApprovalPolicy.execute(eq(PROFESSIONAL_ID), any()))
                .thenThrow(new DomainException("O prazo para responder um pedido deve ser maior que zero."));

        mockMvc.perform(asOwner(json(put("/professionals/{id}/approval-policy", PROFESSIONAL_ID), """
                        {"bookingApproval": "MANUAL", "homeVisitApproval": "MANUAL"}
                        """)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void updatesApprovalPolicy() throws Exception {
        when(updateApprovalPolicy.execute(eq(PROFESSIONAL_ID), any())).thenReturn(professional());

        mockMvc.perform(asOwner(json(put("/professionals/{id}/approval-policy", PROFESSIONAL_ID), """
                        {"bookingApproval": "MANUAL", "homeVisitApproval": "MANUAL", "requestExpirationMinutes": 60}
                        """)))
                .andExpect(status().isOk());

        ArgumentCaptor<ApprovalPolicy> captor = ArgumentCaptor.forClass(ApprovalPolicy.class);
        verify(updateApprovalPolicy).execute(eq(PROFESSIONAL_ID), captor.capture());
        assertThat(captor.getValue()).isEqualTo(new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 60));
    }

    @Test
    void unknownEnumValueBecomes400() throws Exception {
        mockMvc.perform(asOwner(json(put("/professionals/{id}/approval-policy", PROFESSIONAL_ID), """
                        {"bookingApproval": "SOMETIMES", "homeVisitApproval": "MANUAL"}
                        """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
        verifyNoInteractions(updateApprovalPolicy);
    }

    @Test
    void updatesHomeVisitPolicy() throws Exception {
        when(updateHomeVisitPolicy.execute(eq(PROFESSIONAL_ID), any())).thenReturn(professional());

        mockMvc.perform(asOwner(json(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID), """
                        {"offered": true, "originLatitude": -23.5, "originLongitude": -46.6,
                         "fees": [{"maxDistanceKm": 10, "fee": 20}, {"maxDistanceKm": 5, "fee": 10.5}]}
                        """)))
                .andExpect(status().isOk());

        ArgumentCaptor<HomeVisitPolicy> captor = ArgumentCaptor.forClass(HomeVisitPolicy.class);
        verify(updateHomeVisitPolicy).execute(eq(PROFESSIONAL_ID), captor.capture());
        assertThat(captor.getValue()).isEqualTo(new HomeVisitPolicy(true, new GeoPoint(-23.5, -46.6), List.of(
                new HomeVisitFee(new BigDecimal("5"), Money.of("10.50")),
                new HomeVisitFee(new BigDecimal("10"), Money.of("20")))));
    }

    @Test
    void turningHomeVisitOffNeedsNoOriginOrFees() throws Exception {
        when(updateHomeVisitPolicy.execute(eq(PROFESSIONAL_ID), any())).thenReturn(professional());

        mockMvc.perform(asOwner(json(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID),
                        "{\"offered\": false}")))
                .andExpect(status().isOk());

        verify(updateHomeVisitPolicy).execute(PROFESSIONAL_ID, HomeVisitPolicy.notOffered());
    }

    @Test
    void invalidFeeBandIsRefused() throws Exception {
        mockMvc.perform(asOwner(json(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID), """
                        {"offered": true, "originLatitude": 91, "originLongitude": 0,
                         "fees": [{"maxDistanceKm": 0, "fee": 1.234}]}
                        """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("fees[0].fee", "fees[0].maxDistanceKm", "originLatitude")));
        verifyNoInteractions(updateHomeVisitPolicy);
    }

    @Test
    void ownerDeactivatesProfile() throws Exception {
        when(deactivateProfessional.execute(PROFESSIONAL_ID)).thenReturn(professional());

        mockMvc.perform(asOwner(post("/professionals/{id}/deactivate", PROFESSIONAL_ID)))
                .andExpect(status().isOk());
        verify(deactivateProfessional).execute(PROFESSIONAL_ID);
    }

    @Test
    void ownerSeesAgendaOfThePeriod() throws Exception {
        Instant from = Instant.parse("2026-10-13T03:00:00Z");
        Instant to = Instant.parse("2026-10-14T03:00:00Z");
        when(listAgenda.execute(PROFESSIONAL_ID, from, to)).thenReturn(List.of(appointment(AppointmentStatus.PENDING)));

        mockMvc.perform(asOwner(get("/professionals/{id}/agenda", PROFESSIONAL_ID)
                        .param("from", from.toString()).param("to", to.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].startAt").value("2026-10-13T13:00:00Z"));
    }

    @Test
    void agendaRequiresPeriodAndOwner() throws Exception {
        mockMvc.perform(asOwner(get("/professionals/{id}/agenda", PROFESSIONAL_ID).param("from", "2026-10-13T03:00:00Z")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Informe o parâmetro 'to'."));
        mockMvc.perform(get("/professionals/{id}/agenda", PROFESSIONAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L))
                        .param("from", "2026-10-13T03:00:00Z").param("to", "2026-10-14T03:00:00Z"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(listAgenda);
    }
}
