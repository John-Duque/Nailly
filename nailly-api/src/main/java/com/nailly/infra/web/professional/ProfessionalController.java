package com.nailly.infra.web.professional;

import com.nailly.application.appointment.ListProfessionalAgendaUseCase;
import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.infra.security.CurrentUser;
import com.nailly.infra.web.appointment.AppointmentResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/** Perfil público da profissional e as alterações que só ela pode fazer. O cadastro fica em /auth. */
@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final GetProfessionalUseCase getProfessional;
    private final UpdateProfessionalProfileUseCase updateProfile;
    private final UpdateApprovalPolicyUseCase updateApprovalPolicy;
    private final UpdateHomeVisitPolicyUseCase updateHomeVisitPolicy;
    private final DeactivateProfessionalUseCase deactivateProfessional;
    private final ListProfessionalAgendaUseCase listAgenda;

    public ProfessionalController(GetProfessionalUseCase getProfessional,
                                  UpdateProfessionalProfileUseCase updateProfile,
                                  UpdateApprovalPolicyUseCase updateApprovalPolicy,
                                  UpdateHomeVisitPolicyUseCase updateHomeVisitPolicy,
                                  DeactivateProfessionalUseCase deactivateProfessional,
                                  ListProfessionalAgendaUseCase listAgenda) {
        this.getProfessional = getProfessional;
        this.updateProfile = updateProfile;
        this.updateApprovalPolicy = updateApprovalPolicy;
        this.updateHomeVisitPolicy = updateHomeVisitPolicy;
        this.deactivateProfessional = deactivateProfessional;
        this.listAgenda = listAgenda;
    }

    @GetMapping("/{id}")
    public ProfessionalResponse get(@PathVariable long id) {
        return ProfessionalResponse.from(getProfessional.execute(id));
    }

    @PutMapping("/{id}/profile")
    public ProfessionalResponse updateProfile(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                              @Valid @RequestBody ProfessionalProfileRequest request) {
        CurrentUser.of(jwt).requireProfessional(id);
        return ProfessionalResponse.from(
                updateProfile.execute(id, request.getName(), request.getPhone(), request.getBio()));
    }

    @PutMapping("/{id}/approval-policy")
    public ProfessionalResponse updateApprovalPolicy(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                                     @Valid @RequestBody ApprovalPolicyRequest request) {
        CurrentUser.of(jwt).requireProfessional(id);
        return ProfessionalResponse.from(updateApprovalPolicy.execute(id, request.toDomain()));
    }

    @PutMapping("/{id}/home-visit-policy")
    public ProfessionalResponse updateHomeVisitPolicy(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                                      @Valid @RequestBody HomeVisitPolicyRequest request) {
        CurrentUser.of(jwt).requireProfessional(id);
        return ProfessionalResponse.from(updateHomeVisitPolicy.execute(id, request.toDomain()));
    }

    @PostMapping("/{id}/deactivate")
    public ProfessionalResponse deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        CurrentUser.of(jwt).requireProfessional(id);
        return ProfessionalResponse.from(deactivateProfessional.execute(id));
    }

    /** Agenda em [from, to), datas em UTC (ISO-8601), no máximo 31 dias, com todos os status. */
    @GetMapping("/{id}/agenda")
    public List<AppointmentResponse> agenda(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                            @RequestParam Instant from, @RequestParam Instant to) {
        CurrentUser.of(jwt).requireProfessional(id);
        return listAgenda.execute(id, from, to).stream().map(AppointmentResponse::from).toList();
    }
}
