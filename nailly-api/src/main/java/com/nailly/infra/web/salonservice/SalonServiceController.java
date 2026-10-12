package com.nailly.infra.web.salonservice;

import com.nailly.application.salonservice.CreateSalonServiceUseCase;
import com.nailly.application.salonservice.DeactivateSalonServiceUseCase;
import com.nailly.application.salonservice.ListProfessionalServicesUseCase;
import com.nailly.application.salonservice.UpdateSalonServiceUseCase;
import com.nailly.domain.salonservice.SalonService;
import com.nailly.infra.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Catálogo de serviços de uma profissional. Consulta pública; só a própria profissional altera. */
@RestController
@RequestMapping("/professionals/{professionalId}/services")
public class SalonServiceController {

    private final CreateSalonServiceUseCase createService;
    private final UpdateSalonServiceUseCase updateService;
    private final DeactivateSalonServiceUseCase deactivateService;
    private final ListProfessionalServicesUseCase listServices;

    public SalonServiceController(CreateSalonServiceUseCase createService,
                                  UpdateSalonServiceUseCase updateService,
                                  DeactivateSalonServiceUseCase deactivateService,
                                  ListProfessionalServicesUseCase listServices) {
        this.createService = createService;
        this.updateService = updateService;
        this.deactivateService = deactivateService;
        this.listServices = listServices;
    }

    @GetMapping
    public List<SalonServiceResponse> list(@PathVariable long professionalId) {
        return listServices.execute(professionalId).stream().map(SalonServiceResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<SalonServiceResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable long professionalId,
                                                       @Valid @RequestBody SalonServiceRequest request) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        SalonService service = createService.execute(professionalId, request.toCommand());
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(service.getId()).toUri())
                .body(SalonServiceResponse.from(service));
    }

    @PutMapping("/{serviceId}")
    public SalonServiceResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                       @PathVariable long serviceId, @Valid @RequestBody SalonServiceRequest request) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        return SalonServiceResponse.from(updateService.execute(professionalId, serviceId, request.toCommand()));
    }

    @PostMapping("/{serviceId}/deactivate")
    public SalonServiceResponse deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable long professionalId,
                                           @PathVariable long serviceId) {
        CurrentUser.of(jwt).requireProfessional(professionalId);
        return SalonServiceResponse.from(deactivateService.execute(professionalId, serviceId));
    }
}
