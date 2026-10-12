package com.nailly.infra.web.appointment;

import com.nailly.application.appointment.CancelAppointmentUseCase;
import com.nailly.application.appointment.CompleteAppointmentUseCase;
import com.nailly.application.appointment.ConfirmAppointmentUseCase;
import com.nailly.application.appointment.GetAppointmentUseCase;
import com.nailly.application.appointment.MarkAppointmentNoShowUseCase;
import com.nailly.application.appointment.RejectAppointmentUseCase;
import com.nailly.application.appointment.RequestAppointmentUseCase;
import com.nailly.domain.appointment.Appointment;
import com.nailly.infra.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Agendamentos. A cliente pede e pode cancelar; a profissional confirma, recusa, conclui,
 * marca falta e também pode cancelar. Ninguém vê agendamento do qual não participa.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final RequestAppointmentUseCase requestAppointment;
    private final GetAppointmentUseCase getAppointment;
    private final ConfirmAppointmentUseCase confirmAppointment;
    private final RejectAppointmentUseCase rejectAppointment;
    private final CancelAppointmentUseCase cancelAppointment;
    private final CompleteAppointmentUseCase completeAppointment;
    private final MarkAppointmentNoShowUseCase markNoShow;

    public AppointmentController(RequestAppointmentUseCase requestAppointment,
                                 GetAppointmentUseCase getAppointment,
                                 ConfirmAppointmentUseCase confirmAppointment,
                                 RejectAppointmentUseCase rejectAppointment,
                                 CancelAppointmentUseCase cancelAppointment,
                                 CompleteAppointmentUseCase completeAppointment,
                                 MarkAppointmentNoShowUseCase markNoShow) {
        this.requestAppointment = requestAppointment;
        this.getAppointment = getAppointment;
        this.confirmAppointment = confirmAppointment;
        this.rejectAppointment = rejectAppointment;
        this.cancelAppointment = cancelAppointment;
        this.completeAppointment = completeAppointment;
        this.markNoShow = markNoShow;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> request(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody RequestAppointmentRequest request) {
        Appointment appointment = requestAppointment.execute(request.toCommand(CurrentUser.of(jwt).customerId()));
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(appointment.getId()).toUri())
                .body(AppointmentResponse.from(appointment));
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return AppointmentResponse.from(participating(jwt, id));
    }

    @PostMapping("/{id}/confirm")
    public AppointmentResponse confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        requireOwnerProfessional(jwt, id);
        return AppointmentResponse.from(confirmAppointment.execute(id));
    }

    @PostMapping("/{id}/reject")
    public AppointmentResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                      @Valid @RequestBody(required = false) ReasonRequest request) {
        requireOwnerProfessional(jwt, id);
        return AppointmentResponse.from(rejectAppointment.execute(id, reasonOf(request)));
    }

    /** Não há exclusão: cancelar muda o status para CANCELED e mantém o histórico. */
    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                      @Valid @RequestBody(required = false) ReasonRequest request) {
        participating(jwt, id);
        return AppointmentResponse.from(cancelAppointment.execute(id, reasonOf(request)));
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        requireOwnerProfessional(jwt, id);
        return AppointmentResponse.from(completeAppointment.execute(id));
    }

    @PostMapping("/{id}/no-show")
    public AppointmentResponse markNoShow(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        requireOwnerProfessional(jwt, id);
        return AppointmentResponse.from(markNoShow.execute(id));
    }

    /** O agendamento, se quem está logado é a cliente ou a profissional dele. */
    private Appointment participating(Jwt jwt, long id) {
        Appointment appointment = getAppointment.execute(id);
        CurrentUser.of(jwt).requireParticipant(appointment.getCustomerId(), appointment.getProfessionalId());
        return appointment;
    }

    private void requireOwnerProfessional(Jwt jwt, long id) {
        CurrentUser.of(jwt).requireProfessional(getAppointment.execute(id).getProfessionalId());
    }

    private static String reasonOf(ReasonRequest request) {
        return request == null ? null : request.getReason();
    }
}
