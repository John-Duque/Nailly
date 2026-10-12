package com.nailly.infra.web.appointment;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.CompleteAppointmentUseCase;
import com.nailly.application.appointment.ConfirmAppointmentUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.MarkAppointmentNoShowUseCase;
import com.nailly.application.appointment.RejectAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentCommand;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.domain.appointment.AppointmentStatus;
import com.nailly.domain.appointment.ServiceMode;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.shared.GeoPoint;
import com.nailly.infra.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nailly.infra.web.WebTestData.APPOINTMENT_ID;
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
@Import(SecurityConfig.class)
class AppointmentControllerTest {

    private static final String AT_LOCATION = """
            {"customerId": 20, "professionalId": 10, "serviceIds": [5, 6],
             "startAt": "2026-10-13T13:00:00Z", "mode": "AT_LOCATION", "locationId": 3,
             "address": "Rua A, 10", "notes": "Primeira vez"}
            """;

    @Autowired
    private MockMvc mockMvc;

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

    private RequestAppointmentCommand capturedCommand() {
        ArgumentCaptor<RequestAppointmentCommand> captor = ArgumentCaptor.forClass(RequestAppointmentCommand.class);
        verify(requestAppointment).execute(captor.capture());
        return captor.getValue();
    }

    @Test
    void requestsAppointmentAtLocation() throws Exception {
        when(requestAppointment.execute(any())).thenReturn(appointment(AppointmentStatus.CONFIRMED));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(AT_LOCATION))
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
        assertThat(command.getCustomerId()).isEqualTo(20L);
        assertThat(command.getProfessionalId()).isEqualTo(10L);
        assertThat(command.getServiceIds()).containsExactly(5L, 6L);
        assertThat(command.getStartAt()).isEqualTo(START);
        assertThat(command.getMode()).isEqualTo(ServiceMode.AT_LOCATION);
        assertThat(command.getLocationId()).isEqualTo(3L);
        assertThat(command.getNotes()).isEqualTo("Primeira vez");
    }

    @Test
    void requestsHomeVisitWithCustomerCoordinates() throws Exception {
        when(requestAppointment.execute(any())).thenReturn(appointment(AppointmentStatus.PENDING));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId": 20, "professionalId": 10, "serviceIds": [5],
                         "startAt": "2026-10-13T13:00:00Z", "mode": "HOME_VISIT", "customerAddressId": 4,
                         "address": "Rua B, 20", "latitude": -23.56, "longitude": -46.65}
                        """))
                .andExpect(status().isCreated());

        RequestAppointmentCommand command = capturedCommand();
        assertThat(command.getMode()).isEqualTo(ServiceMode.HOME_VISIT);
        assertThat(command.getCustomerAddressId()).isEqualTo(4L);
        assertThat(command.getAddress()).isEqualTo("Rua B, 20");
        assertThat(command.getDestination()).isEqualTo(new GeoPoint(-23.56, -46.65));
    }

    @Test
    void homeVisitWithoutCoordinatesReachesUseCaseWithoutDestination() throws Exception {
        when(requestAppointment.execute(any()))
                .thenThrow(new DomainException("Informe a localização do endereço da cliente."));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId": 20, "professionalId": 10, "serviceIds": [5],
                         "startAt": "2026-10-13T13:00:00Z", "mode": "HOME_VISIT", "customerAddressId": 4,
                         "address": "Rua B, 20", "latitude": -23.56}
                        """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Informe a localização do endereço da cliente."));
        assertThat(capturedCommand().getDestination()).isNull();
    }

    @Test
    void invalidRequestIsRefused() throws Exception {
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId": 20, "professionalId": 10, "serviceIds": [],
                         "address": "Rua A", "latitude": 100}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(containsInAnyOrder("latitude", "mode", "serviceIds", "startAt")));
        verifyNoInteractions(requestAppointment);
    }

    @Test
    void invalidDateFormatIsRefused() throws Exception {
        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(AT_LOCATION.replace("2026-10-13T13:00:00Z", "13/10/2026 10:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
        verifyNoInteractions(requestAppointment);
    }

    @Test
    void missingRelatedRecordBecomes409() throws Exception {
        when(requestAppointment.execute(any())).thenThrow(new DataIntegrityViolationException("fk_appointments_customer"));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(AT_LOCATION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Algum registro informado não existe ou já está cadastrado."));
    }

    @Test
    void unexpectedErrorDoesNotLeakDetails() throws Exception {
        when(requestAppointment.execute(any())).thenThrow(new IllegalStateException("conexão perdida com 10.0.0.5"));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(AT_LOCATION))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Erro inesperado"))
                .andExpect(jsonPath("$.detail")
                        .value("Não foi possível concluir a operação. Tente novamente mais tarde."));
    }

    @Test
    void getsAppointment() throws Exception {
        when(getAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.CONFIRMED));

        mockMvc.perform(get("/appointments/{id}", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(20))
                .andExpect(jsonPath("$.professionalId").value(10))
                .andExpect(jsonPath("$.location.locationId").value(3))
                .andExpect(jsonPath("$.items[0].durationMinutes").value(60));
    }

    @Test
    void changesStatus() throws Exception {
        when(confirmAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.CONFIRMED));
        when(completeAppointment.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.COMPLETED));
        when(markNoShow.execute(APPOINTMENT_ID)).thenReturn(appointment(AppointmentStatus.NO_SHOW));

        mockMvc.perform(post("/appointments/{id}/confirm", APPOINTMENT_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(post("/appointments/{id}/complete", APPOINTMENT_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(post("/appointments/{id}/no-show", APPOINTMENT_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void rejectsWithReason() throws Exception {
        when(rejectAppointment.execute(APPOINTMENT_ID, "Agenda cheia"))
                .thenReturn(appointment(AppointmentStatus.REJECTED));

        mockMvc.perform(post("/appointments/{id}/reject", APPOINTMENT_ID).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Agenda cheia"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void cancelsWithoutBody() throws Exception {
        when(cancelAppointment.execute(APPOINTMENT_ID, null)).thenReturn(appointment(AppointmentStatus.CANCELED));

        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
        verify(cancelAppointment).execute(APPOINTMENT_ID, null);
    }

    @Test
    void tooLongReasonIsRefused() throws Exception {
        String reason = "x".repeat(256);

        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"" + reason + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("reason"));
        verifyNoInteractions(cancelAppointment);
    }

    @Test
    void finishedAppointmentCannotBeCanceled() throws Exception {
        when(cancelAppointment.execute(APPOINTMENT_ID, null))
                .thenThrow(new DomainException("Este agendamento não pode mais ser cancelado."));

        mockMvc.perform(post("/appointments/{id}/cancel", APPOINTMENT_ID))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Este agendamento não pode mais ser cancelado."));
    }

    @Test
    void unknownRouteKeeps404() throws Exception {
        mockMvc.perform(get("/appointments/{id}/nothing", APPOINTMENT_ID))
                .andExpect(status().isNotFound());
        verifyNoInteractions(getAppointment);
    }
}
