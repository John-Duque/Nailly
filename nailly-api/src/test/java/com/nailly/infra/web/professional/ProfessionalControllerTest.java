package com.nailly.infra.web.professional;

import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.domain.professional.ApprovalMode;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitFee;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.domain.shared.Money;
import com.nailly.domain.shared.NotFoundException;
import com.nailly.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static com.nailly.infra.web.WebTestData.professional;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.assertj.core.api.Assertions.assertThat;
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
@Import(SecurityConfig.class)
class ProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterProfessionalUseCase registerProfessional;
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

    @Test
    void registersProfessional() throws Exception {
        when(registerProfessional.execute(7L, "Ana", "11999990000", null)).thenReturn(professional());

        mockMvc.perform(post("/professionals").contentType(MediaType.APPLICATION_JSON).content("""
                        {"userId": 7, "name": "Ana", "phone": "11999990000"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/professionals/" + PROFESSIONAL_ID))
                .andExpect(jsonPath("$.id").value(PROFESSIONAL_ID))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.approvalPolicy.bookingApproval").value("AUTOMATIC"))
                .andExpect(jsonPath("$.approvalPolicy.requestExpirationMinutes").value(30))
                .andExpect(jsonPath("$.homeVisitPolicy.offered").value(true))
                .andExpect(jsonPath("$.homeVisitPolicy.originLatitude").value(-23.55))
                .andExpect(jsonPath("$.homeVisitPolicy.fees[0].maxDistanceKm").value(5.0))
                .andExpect(jsonPath("$.homeVisitPolicy.fees[0].fee").value(10.0));
    }

    @Test
    void invalidBodyListsEveryField() throws Exception {
        mockMvc.perform(post("/professionals").contentType(MediaType.APPLICATION_JSON).content("""
                        {"userId": 0, "name": " ", "phone": "123456789012345678901"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Informe o nome."))
                .andExpect(jsonPath("$.errors[1].field").value("phone"))
                .andExpect(jsonPath("$.errors[2].field").value("userId"))
                .andExpect(jsonPath("$.errors[2].message").value("Usuário inválido."));
        verifyNoInteractions(registerProfessional);
    }

    @Test
    void businessRuleBecomes422() throws Exception {
        when(registerProfessional.execute(7L, "Ana", null, null))
                .thenThrow(new DomainException("Este usuário já tem um perfil de profissional."));

        mockMvc.perform(post("/professionals").contentType(MediaType.APPLICATION_JSON).content("""
                        {"userId": 7, "name": "Ana"}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detail").value("Este usuário já tem um perfil de profissional."));
    }

    @Test
    void getsProfessional() throws Exception {
        when(getProfessional.execute(PROFESSIONAL_ID)).thenReturn(professional());

        mockMvc.perform(get("/professionals/{id}", PROFESSIONAL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.bio").value("Nail designer"));
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
    void updatesProfile() throws Exception {
        when(updateProfile.execute(PROFESSIONAL_ID, "Ana Paula", null, "Bio")).thenReturn(professional());

        mockMvc.perform(put("/professionals/{id}/profile", PROFESSIONAL_ID).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ana Paula", "bio": "Bio"}
                                """))
                .andExpect(status().isOk());
        verify(updateProfile).execute(PROFESSIONAL_ID, "Ana Paula", null, "Bio");
    }

    @Test
    void updatesApprovalPolicy() throws Exception {
        when(updateApprovalPolicy.execute(eq(PROFESSIONAL_ID), any())).thenReturn(professional());

        mockMvc.perform(put("/professionals/{id}/approval-policy", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"bookingApproval": "MANUAL", "homeVisitApproval": "MANUAL", "requestExpirationMinutes": 60}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<ApprovalPolicy> captor = ArgumentCaptor.forClass(ApprovalPolicy.class);
        verify(updateApprovalPolicy).execute(eq(PROFESSIONAL_ID), captor.capture());
        assertThat(captor.getValue()).isEqualTo(new ApprovalPolicy(ApprovalMode.MANUAL, ApprovalMode.MANUAL, 60));
    }

    @Test
    void unknownEnumValueBecomes400() throws Exception {
        mockMvc.perform(put("/professionals/{id}/approval-policy", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"bookingApproval": "SOMETIMES", "homeVisitApproval": "MANUAL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
        verifyNoInteractions(updateApprovalPolicy);
    }

    @Test
    void updatesHomeVisitPolicy() throws Exception {
        when(updateHomeVisitPolicy.execute(eq(PROFESSIONAL_ID), any())).thenReturn(professional());

        mockMvc.perform(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"offered": true, "originLatitude": -23.5, "originLongitude": -46.6,
                                 "fees": [{"maxDistanceKm": 10, "fee": 20}, {"maxDistanceKm": 5, "fee": 10.5}]}
                                """))
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

        mockMvc.perform(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"offered": false}
                                """))
                .andExpect(status().isOk());

        verify(updateHomeVisitPolicy).execute(PROFESSIONAL_ID, HomeVisitPolicy.notOffered());
    }

    @Test
    void invalidFeeBandIsRefused() throws Exception {
        mockMvc.perform(put("/professionals/{id}/home-visit-policy", PROFESSIONAL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"offered": true, "originLatitude": 91, "originLongitude": 0,
                                 "fees": [{"maxDistanceKm": 0, "fee": 1.234}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder(
                                "fees[0].fee", "fees[0].maxDistanceKm", "originLatitude")));
        verifyNoInteractions(updateHomeVisitPolicy);
    }

    @Test
    void deactivatesProfessional() throws Exception {
        when(deactivateProfessional.execute(PROFESSIONAL_ID)).thenReturn(professional());

        mockMvc.perform(post("/professionals/{id}/deactivate", PROFESSIONAL_ID))
                .andExpect(status().isOk());
        verify(deactivateProfessional).execute(PROFESSIONAL_ID);
    }
}
