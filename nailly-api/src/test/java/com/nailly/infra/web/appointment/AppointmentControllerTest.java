package com.nailly.infra.web.appointment;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.CompleteAppointmentUseCase;
import com.nailly.application.appointment.ConfirmAppointmentUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.MarkAppointmentNoShowUseCase;
import com.nailly.application.appointment.RejectAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.application.user.AuthenticatedAccount;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.Role;
import com.nailly.infra.config.ClockConfig;
import com.nailly.infra.security.JwtConfig;
import com.nailly.infra.security.ProblemDetailsSecurityHandler;
import com.nailly.infra.security.SecurityConfig;
import com.nailly.infra.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.nailly.infra.web.WebTestData.APPOINTMENT_ID;
import static com.nailly.infra.web.WebTestData.PROFESSIONAL_ID;
import static com.nailly.infra.web.WebTestData.START;
import static com.nailly.infra.web.WebTestData.appointment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@ActiveProfiles("test")
// Segurança real: mesmas regras de acesso e tokens assinados pela mesma chave da aplicação
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class, ProblemDetailsSecurityHandler.class,
        ClockConfig.class})
class AppointmentControllerTest {

    /** A cliente do agendamento de exemplo (WebTestData.appointment). */
    private static final long CUSTOMER_ID = 20L;
    private static final String AT_LOCATION = """
            {"professionalId": 10, "serviceIds": [5, 6], "startAt": "2026-10-13T13:00:00Z",
             "mode": "AT_LOCATION", "locationId": 3, "notes": "Primeira vez"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;

    @MockitoBean
    private RequestAppointmentUseCase requestAppointment;
    @MockitoBean
    private GetAppointmentUseCase getAppointment;
    @MockitoBean
    private ConfirmAppointmentUseCase confirmAppointment;
    @MockitoBean
    private RejectAppointmentUseCase rejectAppointment;
    @MockitoBean
    private CancelAppointmentUseCase cancelAppointment;
    @MockitoBean
    private CompleteAppointmentUseCase completeAppointment;
    @MockitoBean
    private MarkAppointmentNoShowUseCase markNoShow;

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

    @BeforeEach
    void storedAppointment() {
        when(getAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.CONFIRMED));
    }

    private MockHttpServletRequestBuilder asCustomer(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, customerToken(CUSTOMER_ID));
    }

    private MockHttpServletRequestBuilder asProfessional(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, professionalToken(PROFESSIONAL_ID));
    }

    private MockHttpServletRequestBuilder newRequest(String body) {
        return asCustomer(post("/appointments")).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private RequestAppointmentCommand capturedCommand() {
        ArgumentCaptor<RequestAppointmentCommand> captor = ArgumentCaptor.forClass(RequestAppointmentCommand.class);
        verify(requestAppointment).execute(captor.capture());
        return captor.getValue();
    }

    @Test
    void customerRequestsAppointmentAtLocation() throws Exception {
        when(requestAppointment.execute(any())).thenReturn(appointment(AppointmentStatus.CONFIRMED));

        mockMvc.perform(newRequest(AT_LOCATION))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/appointments/" + APPOINTMENT_ID))
                .andExpect(jsonPath("$.id").value(APPOINTMENT_ID))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.startAt").value("2026-10-13T13:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2026-10-13T14:00:00Z"))
                .andExpect(jsonPath("$.location.mode").value("AT_LOCATION"))
                .andExpect(jsonPath("$.location.address").value("Rua A, 10"))
                .andExpect(jsonPath("$.items[0].serviceTitle").value("Gel nas mãos"))
                .andExpect(jsonPath("$.items[0].procedureTypeName").value("Esmaltação em gel"))
                .andExpect(jsonPath("$.items[0].price").value(80.5))
                .andExpect(jsonPath("$.totalPrice").value(80.5))
                .andExpect(jsonPath("$.homeVisitFee").value(0))
                .andExpect(jsonPath("$.decidedAt").value("2026-10-12T12:00:00Z"));

        RequestAppointmentCommand command = capturedCommand();
        assertThat(command.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(command.getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(command.getServiceIds()).containsExactly(5L, 6L);
        assertThat(command.getStartAt()).isEqualTo(START);
        assertThat(command.getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(command.getLocationId()).isEqualTo(3L);
        assertThat(command.getNotes()).isEqualTo("Primeira vez");
    }

    @Test
    void customerRequestsHomeVisitAtRegisteredAddress() throws Exception {
        when(requestAppointment.execute(any())).thenReturn(appointment(AppointmentStatus.PENDING));

        mockMvc.perform(newRequest("""
                        {"professionalId": 10, "serviceIds": [5], "startAt": "2026-10-13T13:00:00Z",
                         "mode": "HOME_VISIT", "customerAddressId": 4}
                        """))
                .andExpect(status().isCreated());

        RequestAppointmentCommand command = capturedCommand();
        assertThat(command.getMode()).isEqualTo(ServiceMode.HOME_VISIT);
        assertThat(command.getCustomerAddressId()).isEqualTo(4L);
    }

    @Test
    void onlyCustomersRequestAppointments() throws Exception {
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(AT_LOCATION))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(asProfessional(post("/appointments")).contentType(MediaType.APPLICATION_JSON)
                        .content(AT_LOCATION))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Acesso permitido apenas para clientes."));
        verifyNoInteractions(requestAppointment);
    }

    @Test
    void invalidRequestIsRefused() throws Exception {
        mockMvc.perform(newRequest("""
                        {"professionalId": 10, "serviceIds": [], "locationId": 0}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("locationId", "mode", "serviceIds", "startAt")));
        verifyNoInteractions(requestAppointment);
    }

    @Test
    void invalidDateFormatIsRefused() throws Exception {
        mockMvc.perform(newRequest(AT_LOCATION.replace("2026-10-13T13:00:00Z", "13/10/2026 10:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
        verifyNoInteractions(requestAppointment);
    }

    @Test
    void businessRuleBecomes422() throws Exception {
        when(requestAppointment.execute(any()))
                .thenThrow(new DomainException("O horário escolhido está fora do horário de atendimento."));

        mockMvc.perform(newRequest(AT_LOCATION))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O horário escolhido está fora do horário de atendimento."));
    }

    @Test
    void missingRelatedRecordBecomes409() throws Exception {
        when(requestAppointment.execute(any())).thenThrow(new DataIntegrityViolationException("fk"));

        mockMvc.perform(newRequest(AT_LOCATION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Algum registro informado não existe ou já está cadastrado."));
    }

    @Test
    void unexpectedErrorDoesNotLeakDetails() throws Exception {
        when(requestAppointment.execute(any())).thenThrow(new IllegalStateException("conexão perdida com 10.0.0.5"));

        mockMvc.perform(newRequest(AT_LOCATION))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Erro inesperado"))
                .andExpect(jsonPath("$.detail")
                        .value("Não foi possível concluir a operação. Tente novamente mais tarde."));
    }

    @Test
    void participantsSeeTheAppointment() throws Exception {
        mockMvc.perform(asCustomer(get("/appointments/{id}", APPOINTMENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID))
                .andExpect(jsonPath("$.location.locationId").value(3))
                .andExpect(jsonPath("$.items[0].durationMinutes").value(60));
        mockMvc.perform(asProfessional(get("/appointments/{id}", APPOINTMENT_ID)))
                .andExpect(status().isOk());
    }

    @Test
    void othersCannotSeeTheAppointment() throws Exception {
        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, customerToken(21L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Você não participa deste agendamento."));
        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void professionalDecidesAndFinishes() throws Exception {
        when(confirmAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.CONFIRMED));
        when(completeAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.COMPLETED));
        when(markNoShow.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.NO_SHOW));
        when(rejectAppointment.execute(APPOINTMENT_ID, "Agenda cheia"))
                .thenReturn(appointment(AppointmentStatus.REJECTED));

        mockMvc.perform(asProfessional(post("/appointments/{id}/confirm", APPOINTMENT_ID)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(asProfessional(post("/appointments/{id}/complete", APPOINTMENT_ID)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(asProfessional(post("/appointments/{id}/no-show", APPOINTMENT_ID)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NO_SHOW"));
        mockMvc.perform(asProfessional(post("/appointments/{id}/reject", APPOINTMENT_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Agenda cheia\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void customerCannotDecideForTheProfessional() throws Exception {
        mockMvc.perform(asCustomer(post("/appointments/{id}/confirm", APPOINTMENT_ID)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/appointments/{id}/confirm", APPOINTMENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, professionalToken(99L)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(confirmAppointment);
    }

    @Test
    void bothParticipantsCanCancel() throws Exception {
        when(cancelAppointment.execute(APPOINTMENT_ID, null)).thenReturn(appointment(AppointmentStatus.CANCELED));
        when(cancelAppointment.execute(APPOINTMENT_ID, "Imprevisto"))
                .thenReturn(appointment(AppointmentStatus.CANCELED));

        mockMvc.perform(asCustomer(post("/appointments/{id}/cancel", APPOINTMENT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
        mockMvc.perform(asProfessional(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Imprevisto\"}"))
                .andExpect(status().isOk());
        verify(cancelAppointment).execute(APPOINTMENT_ID, null);
        verify(cancelAppointment).execute(APPOINTMENT_ID, "Imprevisto");
    }

    @Test
    void tooLongReasonIsRefused() throws Exception {
        mockMvc.perform(asCustomer(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"" + "x".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("reason"));
        verifyNoInteractions(cancelAppointment);
    }

    @Test
    void unknownRouteKeeps404() throws Exception {
        mockMvc.perform(asCustomer(get("/appointments/{id}/nothing", APPOINTMENT_ID)))
                .andExpect(status().isNotFound());
    }
}
