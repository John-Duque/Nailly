package com.nailly.infra.web.professional;

import com.nailly.application.professional.DeactivateProfessionalUseCase;
import com.nailly.application.professional.GetProfessionalUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.application.professional.UpdateApprovalPolicyUseCase;
import com.nailly.application.professional.UpdateHomeVisitPolicyUseCase;
import com.nailly.application.professional.UpdateProfessionalProfileUseCase;
import com.nailly.domain.professional.Professional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final RegisterProfessionalUseCase registerProfessional;
    private final GetProfessionalUseCase getProfessional;
    private final UpdateProfessionalProfileUseCase updateProfile;
    private final UpdateApprovalPolicyUseCase updateApprovalPolicy;
    private final UpdateHomeVisitPolicyUseCase updateHomeVisitPolicy;
    private final DeactivateProfessionalUseCase deactivateProfessional;

    public ProfessionalController(RegisterProfessionalUseCase registerProfessional,
                                  GetProfessionalUseCase getProfessional,
                                  UpdateProfessionalProfileUseCase updateProfile,
                                  UpdateApprovalPolicyUseCase updateApprovalPolicy,
                                  UpdateHomeVisitPolicyUseCase updateHomeVisitPolicy,
                                  DeactivateProfessionalUseCase deactivateProfessional) {
        this.registerProfessional = registerProfessional;
        this.getProfessional = getProfessional;
        this.updateProfile = updateProfile;
        this.updateApprovalPolicy = updateApprovalPolicy;
        this.updateHomeVisitPolicy = updateHomeVisitPolicy;
        this.deactivateProfessional = deactivateProfessional;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> register(@Valid @RequestBody RegisterProfessionalRequest request) {
        Professional professional = registerProfessional.execute(request.getUserId(), request.getName(),
                request.getPhone(), request.getBio());
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(professional.getId()).toUri())
                .body(ProfessionalResponse.from(professional));
    }

    @GetMapping("/{id}")
    public ProfessionalResponse get(@PathVariable long id) {
        return ProfessionalResponse.from(getProfessional.execute(id));
    }

    @PutMapping("/{id}/profile")
    public ProfessionalResponse updateProfile(@PathVariable long id,
                                              @Valid @RequestBody ProfessionalProfileRequest request) {
        return ProfessionalResponse.from(
                updateProfile.execute(id, request.getName(), request.getPhone(), request.getBio()));
    }

    @PutMapping("/{id}/approval-policy")
    public ProfessionalResponse updateApprovalPolicy(@PathVariable long id,
                                                     @Valid @RequestBody ApprovalPolicyRequest request) {
        return ProfessionalResponse.from(updateApprovalPolicy.execute(id, request.toDomain()));
    }

    @PutMapping("/{id}/home-visit-policy")
    public ProfessionalResponse updateHomeVisitPolicy(@PathVariable long id,
                                                      @Valid @RequestBody HomeVisitPolicyRequest request) {
        return ProfessionalResponse.from(updateHomeVisitPolicy.execute(id, request.toDomain()));
    }

    @PostMapping("/{id}/deactivate")
    public ProfessionalResponse deactivate(@PathVariable long id) {
        return ProfessionalResponse.from(deactivateProfessional.execute(id));
    }
}
